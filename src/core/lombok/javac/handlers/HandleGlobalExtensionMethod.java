/*
 * Copyright (C) 2023 The Project Lombok Authors.
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
package lombok.javac.handlers;

import java.util.ArrayList;
import java.util.List;

import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;

import lombok.core.AST.Kind;
import lombok.core.HandlerPriority;
import lombok.core.configuration.TypeName;
import lombok.javac.JavacASTAdapter;
import lombok.javac.JavacASTVisitor;
import lombok.javac.JavacNode;
import lombok.javac.handlers.HandleExtensionMethod.Extension;
import lombok.spi.Provides;

@Provides(JavacASTVisitor.class)
@HandlerPriority(66560)
public class HandleGlobalExtensionMethod extends JavacASTAdapter {
	private HandleExtensionMethod handleExtensionMethod = new HandleExtensionMethod();
	
	@Override
	public void visitType(JavacNode typeNode, JCClassDecl type) {
		if (typeNode.up().getKind() != Kind.COMPILATION_UNIT) return;
		
		List<TypeName> configExtensions = typeNode.getAst().readConfiguration(lombok.ConfigurationKeys.GLOBAL_EXTENSION_METHODS);
		if (configExtensions.isEmpty()) return;
		
		JavacElements elementUtils = typeNode.getAst().getElementUtils();
		List<Extension> extensions = new ArrayList<Extension>();
		for (TypeName typeName : configExtensions) {
			ClassSymbol element = elementUtils.getTypeElement(typeName.getName());
			if (element == null) {
				typeNode.addWarning(String.format("Unable to resolve type for global extension method provider '%s'", typeName.getName()));
				continue;
			}
			
			Extension extension = handleExtensionMethod.getExtension(typeNode, (ClassType) element.type);
			extensions.add(extension);
		}
		
		handleExtensionMethod.applyExtensions(typeNode, extensions, false);
	}
}
