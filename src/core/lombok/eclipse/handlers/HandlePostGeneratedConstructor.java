/*
 * Copyright (C) 2020 The Project Lombok Authors.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lombok.eclipse.handlers;

import static lombok.core.handlers.HandlerUtil.handleFlagUsage;
import static lombok.eclipse.Eclipse.pos;
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.ForStatement;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.LambdaExpression;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.SwitchStatement;
import org.eclipse.jdt.internal.compiler.ast.SynchronizedStatement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.ast.WhileStatement;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.mangosdk.spi.ProviderFor;

import lombok.ConfigurationKeys;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.eclipse.DeferUntilPostDiet;
import lombok.eclipse.EclipseAST;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.experimental.PostGeneratedConstructor;

/**
 * Handles the {@code lombok.experimental.PostGeneratedConstructor} annotation for eclipse.
 */
@ProviderFor(EclipseAnnotationHandler.class)
@DeferUntilPostDiet
@HandlerPriority(value = 1024)
public class HandlePostGeneratedConstructor extends EclipseAnnotationHandler<PostGeneratedConstructor> {
	
	@Override public void handle(AnnotationValues<PostGeneratedConstructor> annotation, Annotation source, EclipseNode annotationNode) {
		handleFlagUsage(annotationNode, ConfigurationKeys.POST_GENERATED_CONSTRUCTOR_FLAG_USAGE, "@PostGeneratedConstructor");
		
		EclipseNode methodNode = annotationNode.up();
		if (methodNode == null || methodNode.getKind() != Kind.METHOD || !(methodNode.get() instanceof MethodDeclaration)) {
			annotationNode.addError("@PostGeneratedConstructor is legal only on methods.");
			return;
		}
		
		MethodDeclaration method = (MethodDeclaration) methodNode.get();
		if (method.isAbstract()) {
			annotationNode.addError("@PostGeneratedConstructor is legal only on concrete methods.");
			return;
		}
		
		if (method.isStatic()) {
			annotationNode.addError("@PostGeneratedConstructor is legal only on instance methods.");
			return;
		}
		
		EclipseNode typeNode = upToTypeNode(annotationNode);
		
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = pos(source);
		
		List<AbstractMethodDeclaration> generatedConstructors = findGeneratedConstructors(typeNode);
		if (generatedConstructors.isEmpty()) {
			annotationNode.addError("Cannot find a generated constructor.");
			return;
		}
		
		for (AbstractMethodDeclaration methodDecl : generatedConstructors) {
			MessageSend call = new MessageSend();
			call.selector = method.selector;
			call.receiver = ThisReference.implicitThis();
			
			call.nameSourcePosition = p;
			call.sourceStart = pS;
			call.sourceEnd = call.statementEnd = pE;
			setGeneratedBy(call, source);
			
			for (ReturnStatement returnStatement : findReturnStatements(methodDecl)) {
				Block block = getOrWrapInBlock(returnStatement, typeNode.getAst());
				block.statements = concat(new Statement[] {call}, block.statements, Statement.class);
			}
			
			methodDecl.statements = concat(methodDecl.statements, new Statement[] {call}, Statement.class);
			methodDecl.thrownExceptions = concat(methodDecl.thrownExceptions, method.thrownExceptions, TypeReference.class);
			
			typeNode.getAst().get(methodDecl).rebuild();
		}
	}
	
	private Block getOrWrapInBlock(ReturnStatement returnStatement, EclipseAST ast) {
    	ASTNode astNode = ast.get(returnStatement).directUp().get();
    	if (astNode instanceof Block) {
    		return (Block) astNode;
    	}
    	if (astNode instanceof SynchronizedStatement) {
			return ((SynchronizedStatement) astNode).block;
    	}
    	
    	Block block = new Block(1);
		block.statements = new Statement[] { returnStatement };
    	if (astNode instanceof ForStatement) {
			((ForStatement) astNode).action = block;
    	}
    	if (astNode instanceof WhileStatement) {
			((WhileStatement) astNode).action = block;
    	}
    	if (astNode instanceof IfStatement) {
			IfStatement ifStatement = (IfStatement) astNode;
			if (ifStatement.thenStatement == returnStatement) {
				ifStatement.thenStatement = block;
			} else {
				ifStatement.elseStatement = block;
			}
    	}
    	if (astNode instanceof SwitchStatement) {
			SwitchStatement switchStatement = (SwitchStatement) astNode;
			for (int i = 0; i < switchStatement.statements.length; i++) {
				Statement statement = switchStatement.statements[i];
				if (statement == returnStatement) {
					switchStatement.statements[i] = block;
					break;
				}
			}
    	}
    	return block;
    	
	}

	private List<AbstractMethodDeclaration> findGeneratedConstructors(EclipseNode typeNode) {
		List<AbstractMethodDeclaration> constructors = new ArrayList<AbstractMethodDeclaration>();
		if (typeNode != null && typeNode.get() instanceof TypeDeclaration) {
			TypeDeclaration typeDecl = (TypeDeclaration) typeNode.get();
			for (AbstractMethodDeclaration methodDecl : typeDecl.methods) {
//				if (methodDecl instanceof ConstructorDeclaration && isGenerated(methodDecl)) {
					constructors.add(methodDecl);
//				}
			}
		}
		return constructors;
	}
	
	private List<ReturnStatement> findReturnStatements(AbstractMethodDeclaration methodDecl) {
		List<ReturnStatement> returnStatements = new ArrayList<ReturnStatement>();
		methodDecl.traverse(new ReturnCollector(returnStatements), (ClassScope)null);
		return returnStatements;
	}
	
	static class ReturnCollector extends ASTVisitor {
		private List<ReturnStatement> returnStatements;
		
		ReturnCollector(List<ReturnStatement> returnStatements) {
			this.returnStatements = returnStatements;
		}
		
		@Override
		public boolean visit(TypeDeclaration type, BlockScope skope) {
			return false;
		}
		@Override
		public boolean visit(TypeDeclaration type, ClassScope skope) {
			return false;
		}
		@Override
		public boolean visit(LambdaExpression type, BlockScope skope) {
			return false;
		}
	    @Override
		public boolean visit(ReturnStatement returnStatement, BlockScope skope) {
	    	returnStatements.add(returnStatement);
	    	return false;
	    }
	}
}
