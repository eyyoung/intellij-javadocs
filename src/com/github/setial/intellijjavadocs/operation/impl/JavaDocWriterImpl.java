package com.github.setial.intellijjavadocs.operation.impl;

import com.github.setial.intellijjavadocs.operation.JavaDocWriter;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.application.RunResult;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.openapi.vfs.ReadonlyStatusHandler.OperationStatus;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl;
import com.intellij.psi.javadoc.PsiDocComment;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * The type Java doc writer impl.
 *
 * @author Sergey Timofiychuk
 */
public class JavaDocWriterImpl implements JavaDocWriter {

    /**
     * The constant COMPONENT_NAME.
     */
    public static final String COMPONENT_NAME = "JavaDocWriter";

    @Override
    public void initComponent() {
    }

    @Override
    public void disposeComponent() {
    }

    @NotNull
    @Override
    public String getComponentName() {
        return COMPONENT_NAME;

    }

    @Override
    public void write(@NotNull PsiDocComment javaDoc, @NotNull PsiElement beforeElement) {
        OperationStatus status = ReadonlyStatusHandler.getInstance(beforeElement.getProject()).
                ensureFilesWritable(Arrays.asList(beforeElement.getContainingFile().getVirtualFile()));
        if (status.hasReadonlyFiles()) {
            // TODO show error message
            // TODO stop execution
        }

        WriteCommandAction command = new WriteCommandActionImpl(javaDoc, beforeElement);
        RunResult result = command.execute();

        // TODO check result and show warning if there was some errors
    }

    /**
     * The type Write command action impl.
     *
     * @author Sergey Timofiychuk
     */
    private static class WriteCommandActionImpl extends WriteCommandAction {

        private PsiDocComment javaDoc;
        private PsiElement element;

        /**
         * Instantiates a new Write command action impl.
         *
         * @param javaDoc the Java doc
         * @param element the Element
         */
        public WriteCommandActionImpl(@NotNull PsiDocComment javaDoc, @NotNull PsiElement element) {
            super(
                    element.getProject(),
                    WRITE_JAVADOC_COMMAND_NAME,
                    WRITE_JAVADOC_COMMAND_GROUP,
                    element.getContainingFile());
            this.javaDoc = javaDoc;
            this.element = element;
        }

        @Override
        protected void run(Result result) throws Throwable {
            if (javaDoc == null) {
                // TODO create result object
                return;
            }
            if (element.getFirstChild() instanceof PsiDocComment) {
                element.getFirstChild().replace(javaDoc);
            } else {
                element.getNode().addChild(javaDoc.getNode(), element.getFirstChild().getNode());
            }

            ensureWhitespaceAfterJavaDoc(element);
            CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(element.getProject());
            codeStyleManager.reformatNewlyAddedElement(element.getNode(), element.getFirstChild().getNode());
        }

        private void ensureWhitespaceAfterJavaDoc(PsiElement element) {
            PsiElement firstChild = element.getFirstChild();
            if (!PsiDocComment.class.isAssignableFrom(firstChild.getClass())) {
                return;
            }
            PsiElement nextElement = firstChild.getNextSibling();
            if (PsiWhiteSpace.class.isAssignableFrom(nextElement.getClass())) {
                return;
            }
            element.getNode().addChild(new PsiWhiteSpaceImpl("\n"), nextElement.getNode());
        }
    }
}
