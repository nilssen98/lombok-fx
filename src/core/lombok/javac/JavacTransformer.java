/*
 * Copyright (C) 2009-2023 The Project Lombok Authors.
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
package lombok.javac;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Messager;

import com.sun.source.util.Trees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.Context;

import lombok.ConfigurationKeys;
import lombok.core.CleanupRegistry;
import lombok.core.LombokConfiguration;

public class JavacTransformer {
	private final HandlerLibrary handlers;
	private final Messager messager;
	private final CleanupRegistry cleanup;
	
	public JavacTransformer(Messager messager, Trees trees, CleanupRegistry cleanup) {
		this.messager = messager;
		this.handlers = HandlerLibrary.load(messager, trees);
		this.cleanup = cleanup;
	}
	
	public void transform(Context context, List<JCCompilationUnit> compilationUnits) {
		if (compilationUnits.isEmpty()) {
			return;
		}
		JavacAST.ErrorLog errorLog = JavacAST.ErrorLog.create(messager, context);
		Set<JCCompilationUnit> skip = Collections.newSetFromMap(new IdentityHashMap<JCCompilationUnit, Boolean>());
		// Step 1: Run all normal handlers file by file
		for (JCCompilationUnit unit : compilationUnits) {
			if (Boolean.TRUE.equals(LombokConfiguration.read(ConfigurationKeys.LOMBOK_DISABLE, JavacAST.getAbsoluteFileLocation(unit)))) {
				skip.add(unit);
				continue;
			}
			
			JavacAST ast = new JavacAST(errorLog, context, unit, cleanup);
			for (Long prio : handlers.getPrioritiesRequiringNoResolution()) {
				ast.traverse(new AnnotationVisitor(prio));
				handlers.callASTVisitors(ast, prio);
			}
			if (ast.isChanged()) LombokOptions.markChanged(context, (JCCompilationUnit) ast.top().get());
		}
		
		// Step 2: Run all resolution based handlers priority by priority
		for (Long prio : handlers.getPrioritiesRequiringResolutionReset()) {
			for (JCCompilationUnit unit : compilationUnits) {
				if (skip.contains(unit)) {
					continue;
				}
				
				JavacAST ast = new JavacAST(errorLog, context, unit, cleanup);
				ast.traverse(new AnnotationVisitor(prio));
				handlers.callASTVisitors(ast, prio);
				if (ast.isChanged()) LombokOptions.markChanged(context, (JCCompilationUnit) ast.top().get());
			}
		}
	}
	
	private class AnnotationVisitor extends JavacASTAdapter {
		private final long priority;
		
		AnnotationVisitor(long priority) {
			this.priority = priority;
		}
		
		@Override public void visitAnnotationOnType(JCClassDecl type, JavacNode annotationNode, JCAnnotation annotation) {
			JCCompilationUnit top = (JCCompilationUnit) annotationNode.top().get();
			handlers.handleAnnotation(top, annotationNode, annotation, priority);
		}
		
		@Override public void visitAnnotationOnField(JCVariableDecl field, JavacNode annotationNode, JCAnnotation annotation) {
			JCCompilationUnit top = (JCCompilationUnit) annotationNode.top().get();
			handlers.handleAnnotation(top, annotationNode, annotation, priority);
		}
		
		@Override public void visitAnnotationOnMethod(JCMethodDecl method, JavacNode annotationNode, JCAnnotation annotation) {
			JCCompilationUnit top = (JCCompilationUnit) annotationNode.top().get();
			handlers.handleAnnotation(top, annotationNode, annotation, priority);
		}
		
		@Override public void visitAnnotationOnMethodArgument(JCVariableDecl argument, JCMethodDecl method, JavacNode annotationNode, JCAnnotation annotation) {
			JCCompilationUnit top = (JCCompilationUnit) annotationNode.top().get();
			handlers.handleAnnotation(top, annotationNode, annotation, priority);
		}
		
		@Override public void visitAnnotationOnLocal(JCVariableDecl local, JavacNode annotationNode, JCAnnotation annotation) {
			JCCompilationUnit top = (JCCompilationUnit) annotationNode.top().get();
			handlers.handleAnnotation(top, annotationNode, annotation, priority);
		}
		
		@Override public void visitAnnotationOnTypeUse(JCTree typeUse, JavacNode annotationNode, JCAnnotation annotation) {
			JCCompilationUnit top = (JCCompilationUnit) annotationNode.top().get();
			handlers.handleAnnotation(top, annotationNode, annotation, priority);
		}
	}
}
