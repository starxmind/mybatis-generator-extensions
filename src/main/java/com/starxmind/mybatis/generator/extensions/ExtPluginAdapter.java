package com.starxmind.mybatis.generator.extensions;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 给Java对象生成Lombok风格的Mybatis插件
 *
 * @author pizzalord
 * @since 1.0
 */
public class ExtPluginAdapter extends PluginAdapter {
    private final Collection<Annotations> annotations = new LinkedHashSet(ExtPluginAdapter.Annotations.values().length);

    public ExtPluginAdapter() {
    }

    public boolean validate(List<String> warnings) {
        return true;
    }

    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        topLevelClass.addImportedType("lombok.Data");
        topLevelClass.addImportedType("lombok.Builder");
        topLevelClass.addImportedType("lombok.NoArgsConstructor");
        topLevelClass.addImportedType("lombok.AllArgsConstructor");
        topLevelClass.addAnnotation("@Data");
        topLevelClass.addAnnotation("@Builder");
        topLevelClass.addAnnotation("@NoArgsConstructor");
        topLevelClass.addAnnotation("@AllArgsConstructor");
        topLevelClass.addJavaDocLine("/**");
        topLevelClass.addJavaDocLine(" * Created by Mybatis Generator on " + this.date2Str(new Date()));
        topLevelClass.addJavaDocLine(" */");
        return true;
    }

    private String date2Str(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        return sdf.format(date);
    }

    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.addAnnotations(topLevelClass);
        return true;
    }

    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.addAnnotations(topLevelClass);
        return true;
    }

    public boolean modelGetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, Plugin.ModelClassType modelClassType) {
        return false;
    }

    public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, Plugin.ModelClassType modelClassType) {
        return false;
    }

    private void addAnnotations(TopLevelClass topLevelClass) {
        Iterator var2 = this.annotations.iterator();

        while (var2.hasNext()) {
            Annotations annotation = (Annotations) var2.next();
            topLevelClass.addImportedType(annotation.javaType);
            topLevelClass.addAnnotation(annotation.asAnnotation());
        }

    }

    public void setProperties(Properties properties) {
        super.setProperties(properties);
        this.annotations.add(ExtPluginAdapter.Annotations.DATA);
        Iterator var2 = properties.stringPropertyNames().iterator();

        while (true) {
            String annotationName;
            Annotations annotation;
            String optionsPrefix;
            do {
                do {
                    do {
                        if (!var2.hasNext()) {
                            return;
                        }

                        annotationName = (String) var2.next();
                    } while (annotationName.contains("."));

                    optionsPrefix = properties.getProperty(annotationName);
                } while (!Boolean.parseBoolean(optionsPrefix));

                annotation = ExtPluginAdapter.Annotations.getValueOf(annotationName);
            } while (annotation == null);

            optionsPrefix = annotationName + ".";
            Iterator var7 = properties.stringPropertyNames().iterator();

            while (var7.hasNext()) {
                String propertyName = (String) var7.next();
                if (propertyName.startsWith(optionsPrefix)) {
                    String propertyValue = properties.getProperty(propertyName);
                    annotation.appendOptions(propertyName, propertyValue);
                    this.annotations.add(annotation);
                    this.annotations.addAll(ExtPluginAdapter.Annotations.getDependencies(annotation));
                }
            }
        }
    }

    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Mapper"));
        interfaze.addAnnotation("@Mapper");
        return true;
    }

    private static enum Annotations {
        DATA("data", "@Data", "lombok.Data"),
        BUILDER("builder", "@Builder", "lombok.Builder"),
        ALL_ARGS_CONSTRUCTOR("allArgsConstructor", "@AllArgsConstructor", "lombok.AllArgsConstructor"),
        NO_ARGS_CONSTRUCTOR("noArgsConstructor", "@NoArgsConstructor", "lombok.NoArgsConstructor"),
        ACCESSORS("accessors", "@Accessors", "lombok.experimental.Accessors"),
        TO_STRING("toString", "@ToString", "lombok.ToString");

        private final String paramName;
        private final String name;
        private final FullyQualifiedJavaType javaType;
        private final List<String> options;

        private Annotations(String paramName, String name, String className) {
            this.paramName = paramName;
            this.name = name;
            this.javaType = new FullyQualifiedJavaType(className);
            this.options = new ArrayList();
        }

        private static Annotations getValueOf(String paramName) {
            Annotations[] var1 = values();
            int var2 = var1.length;

            for (int var3 = 0; var3 < var2; ++var3) {
                Annotations annotation = var1[var3];
                if (String.CASE_INSENSITIVE_ORDER.compare(paramName, annotation.paramName) == 0) {
                    return annotation;
                }
            }

            return null;
        }

        private static Collection<Annotations> getDependencies(Annotations annotation) {
            return (Collection) (annotation == ALL_ARGS_CONSTRUCTOR ? Collections.singleton(NO_ARGS_CONSTRUCTOR) : Collections.emptyList());
        }

        private static String quote(String value) {
            return !Boolean.TRUE.toString().equals(value) && !Boolean.FALSE.toString().equals(value) ? value.replaceAll("[\\w]+", "\"$0\"") : value;
        }

        private void appendOptions(String key, String value) {
            String keyPart = key.substring(key.indexOf(".") + 1);
            String valuePart = value.contains(",") ? String.format("{%s}", value) : value;
            this.options.add(String.format("%s=%s", keyPart, quote(valuePart)));
        }

        private String asAnnotation() {
            if (this.options.isEmpty()) {
                return this.name;
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(this.name);
                sb.append("(");
                boolean first = true;

                String option;
                for (Iterator var3 = this.options.iterator(); var3.hasNext(); sb.append(option)) {
                    option = (String) var3.next();
                    if (first) {
                        first = false;
                    } else {
                        sb.append(", ");
                    }
                }

                sb.append(")");
                return sb.toString();
            }
        }
    }
}
