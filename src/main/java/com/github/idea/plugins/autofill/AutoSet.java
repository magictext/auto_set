package com.github.idea.plugins.autofill;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.impl.source.PsiClassImpl;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class AutoSet extends PsiElementBaseIntentionAction implements IntentionAction {
    @Override
    public void invoke(@NotNull final Project project, final Editor editor, @NotNull final PsiElement psiElement) throws IncorrectOperationException {
        System.out.println("invoke call");
        PsiBinaryExpression binaryExpression = findBinaryExpression(psiElement);
        PsiExpression lOperand = binaryExpression.getLOperand();
        PsiExpression rOperand = binaryExpression.getROperand();
        if (lOperand == null || rOperand == null) return;
        // 获取对象所对应的类
        PsiClass lClass = (PsiClassImpl)((PsiClassReferenceType) lOperand.getType()).getReference().resolve();
        // 获取对象所对应的类
        PsiClass rClass = (PsiClassImpl)((PsiClassReferenceType) rOperand.getType()).getReference().resolve();
        if (lClass == null || rClass == null) return;

        String s = setByGet(lOperand.getText(), lClass, rOperand.getText(), rClass);

        Document document = editor.getDocument();
        int lineNumber = document.getLineNumber(psiElement.getTextOffset());
        document.replaceString(document.getLineStartOffset(lineNumber),document.getLineEndOffset(lineNumber),s);
        CodeStyleManager.getInstance(project).reformatText(psiElement.getContainingFile()
                ,document.getLineStartOffset(lineNumber),document.getLineEndOffset(lineNumber+StringUtil.countNewLines(s)+1));

    }

    @Override
    public boolean isAvailable(@NotNull final Project project, final Editor editor, @NotNull final PsiElement element) {
        PsiBinaryExpression binaryExpression = findBinaryExpression(element);
        if (binaryExpression != null){
            return "<=".equals(binaryExpression.getOperationSign().getText())
                    && binaryExpression.getLOperand() instanceof PsiReferenceExpression
                    && binaryExpression.getROperand() instanceof PsiReferenceExpression;
        }
        return false;
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return getText();
    }

    @NotNull
    @Override
    public String getText() {
        return "auto generate setter";
    }

    private PsiBinaryExpression findBinaryExpression(final PsiElement element){
        if (element == null) return null;
        if (element.getPrevSibling() != null && element.getPrevSibling() instanceof PsiExpressionStatement){
            PsiExpressionStatement prevSibling = (PsiExpressionStatement) element.getPrevSibling();
            if (prevSibling != null && prevSibling.getFirstChild() != null && prevSibling.getFirstChild() instanceof PsiBinaryExpression){
                return (PsiBinaryExpression) prevSibling.getFirstChild();
            }
        }
        if (element instanceof PsiBinaryExpression) return (PsiBinaryExpression) element;
        return findBinaryExpression(element.getParent());
    }

    private String setByGet(String leftObjName, PsiClass leftClass, String rightObjName, PsiClass rightClass) {
        PsiField[] leftFields = leftClass.getFields();
        PsiField[] rightFields = rightClass.getFields();
        StringBuilder sb = new StringBuilder();
        for (PsiField leftField : leftFields) {
        for (PsiField rightField : rightFields) {
        if (leftField.getName().equals(rightField.getName()) && leftField.getType().equals(rightField.getType())) {
            PsiMethod getterMethod = getGetterMethod(rightClass, rightField);
            PsiMethod setterMethod = getSetterMethod(leftClass, leftField);
            if (getterMethod != null && setterMethod != null) {
                sb.append(leftObjName)
                        .append(".")
                        .append(setterMethod.getName())
                        .append("(")
                        .append(rightObjName)
                        .append(".")
                        .append(getterMethod.getName())
                        .append("());\n");
            }
            break;
        }
    }
    }
        return sb.toString();
    }
    private PsiMethod getGetterMethod(PsiClass psiClass, PsiField psiField) {
        String getterName = getGetterName(psiField.getName(), psiField.getType().getCanonicalText());
        if (getterName == null) {
            return null;
        }
        PsiMethod[] methods = psiClass.getMethods();
        for (PsiMethod method : methods) {
        if (method.getName().equals(getterName) && method.getParameterList().isEmpty()) {
            PsiType returnType = method.getReturnType();
            if (returnType != null && returnType.equals(psiField.getType())) {
                return method;
            }
        }
    }
        return null;
    }

    private String getGetterName(String fieldName, String fieldType) {
        String getterName = null;
        if (fieldType.equals("boolean") || fieldType.equals("java.lang.Boolean")) {
            getterName = "is" + capitalize(fieldName);
        } else {
            getterName = "get" + capitalize(fieldName);
        }
        return getterName;
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private PsiMethod getSetterMethod(PsiClass psiClass, PsiField psiField) {
        String setterName = getSetterName(psiField.getName(), psiField.getType().getCanonicalText());
        if (setterName == null) {
            return null;
        }
        PsiMethod[] methods = psiClass.getMethods();
        for (PsiMethod method : methods) {
        if (method.getName().equals(setterName) && method.getParameterList().getParametersCount() == 1) {
            PsiParameter parameter = method.getParameterList().getParameters()[0];
            PsiType parameterType = parameter.getType();
            if (parameterType != null && parameterType.equals(psiField.getType())) {
                return method;
            }
        }
    }
        return null;
    }

    private String getSetterName(String fieldName, String fieldType) {
        String setterName = "set" + capitalize(fieldName);
        return setterName;
    }

}
