package com.github.setial.intellijjavadocs.generator.impl;

import com.github.setial.intellijjavadocs.model.JavaDoc;
import com.github.setial.intellijjavadocs.model.settings.GeneralSettings;
import com.github.setial.intellijjavadocs.model.settings.JavaDocSettings;
import com.github.setial.intellijjavadocs.model.settings.Level;
import com.github.setial.intellijjavadocs.utils.JavaDocUtils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiEnumConstant;
import com.intellij.psi.PsiField;
import org.apache.velocity.Template;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * The type Field java doc generator.
 *
 * @author Sergey Timofiychuk
 */
public class FieldJavaDocGenerator extends AbstractJavaDocGenerator<PsiField> {

    /**
     * Instantiates a new Field java doc generator.
     *
     * @param project the Project
     */
    public FieldJavaDocGenerator(@NotNull Project project) {
        super(project);
    }

    @Nullable
    @Override
    protected JavaDoc generateJavaDoc(@NotNull PsiField element) {
        JavaDocSettings configuration = getSettings().getConfiguration();
        GeneralSettings generalSettings = configuration.getGeneralSettings();
        if (configuration != null && !generalSettings.getLevels().contains(Level.FIELD) ||
                !shouldGenerate(element.getModifierList())) {
            return null;
        }
        Template template = getDocTemplateManager().getFieldTemplate(element);
        Map<String, Object> params = getDefaultParameters(element);
        if (PsiEnumConstant.class.isAssignableFrom(element.getClass())) {
            params.put("name", element.getName());
        }
        String date;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(generalSettings.getDateFormat());
            date = sdf.format(new Date());
        } catch (Exception e) {
            date = new Date().toString();
        }
        params.put("date", date);
        params.put("author", generalSettings.getAuthName());
        String javaDocText = getDocTemplateProcessor().merge(template, params);
        return JavaDocUtils.toJavaDoc(javaDocText, getPsiElementFactory());
    }

}
