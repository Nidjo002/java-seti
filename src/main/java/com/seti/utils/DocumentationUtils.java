package com.seti.utils;

import java.awt.Desktop;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public final class DocumentationUtils {

    private static final Logger logger = Logger.getLogger(DocumentationUtils.class.getName());
    private static final Path DOC_FILE = Path.of("docs", "doc.html");
    private static final String CLASSES_PATH = "target/classes/com/seti";
    private static final String PREFIX_TO_SKIP = "target/classes/";
    private static final String CLOSE_SPAN = "</span>";
    private static final String CLOSE_LI = "</li>";
    private static final String OPEN_MODIFIER = "<span class='modifier'>";
    private static final String OPEN_TYPE = "<span class='type'>";
    private static final String OPEN_STRONG = "<strong>";
    private static final String CLOSE_STRONG = "</strong>";
    private static final String HTML_HEADER = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <title>SETI Documentation</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; background: #f5f5f5; color: #333; }
                    h1 { color: #2c3e50; border-bottom: 3px solid #3498db; padding-bottom: 10px; }
                    h2 { color: #2980b9; margin-top: 30px; }
                    .class-section { background: white; padding: 20px; margin: 20px 0;
                                     border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                    .class-name { color: #e74c3c; }
                    .modifier { color: #8e44ad; font-family: monospace; }
                    .type { color: #27ae60; font-family: monospace; }
                    ul { line-height: 1.8; padding-left: 20px; list-style: none; }
                    li { margin: 4px 0; font-family: monospace; font-size: 0.9em; }
                </style>
            </head>
            <body>
            <h1>SETI Board Game — Documentation</h1>
            """;
    private static final String HTML_FOOTER = """
            </body>
            </html>
            """;

    private DocumentationUtils() {}

    public static void openDocumentation() {
        try (var stream = Files.walk(Paths.get(CLASSES_PATH))) {
            var classFiles = stream
                    .filter(p -> p.getFileName().toString().endsWith(".class"))
                    .toList();

            StringBuilder html = new StringBuilder();
            html.append(HTML_HEADER);

            for (Path path : classFiles) {
                String fqn = path.toString()
                        .substring(PREFIX_TO_SKIP.length(), path.toString().length() - ".class".length())
                        .replace("\\", ".")
                        .replace("/", ".");
                Class<?> clazz = Class.forName(fqn);
                html.append(buildClassSection(clazz));
            }

            html.append(HTML_FOOTER);
            saveDocumentation(html.toString());
            Desktop.getDesktop().browse(DOC_FILE.toUri());

        } catch (IOException | ClassNotFoundException e) {
            logger.severe("Failed to generate documentation: " + e.getMessage());
        }
    }

    private static String buildClassSection(Class<?> clazz) {
        if (clazz.isMemberClass() || clazz.isAnonymousClass() || clazz.isLocalClass()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        String classType = resolveClassType(clazz);
        String modifiers = Modifier.toString(clazz.getModifiers())
                .replace("abstract interface", "interface")
                .trim();

        sb.append("<div class='class-section'>");
        sb.append("<h2>")
                .append(OPEN_MODIFIER).append(modifiers).append(CLOSE_SPAN).append(" ")
                .append(classType).append(" <span class='class-name'>")
                .append(clazz.getSimpleName()).append(CLOSE_SPAN);

        if (clazz.getSuperclass() != null
                && !clazz.getSuperclass().equals(Object.class)
                && !clazz.getSuperclass().equals(Record.class)
                && !clazz.getSuperclass().equals(Enum.class)) {
            sb.append(" extends ").append(OPEN_TYPE)
                    .append(clazz.getSuperclass().getSimpleName()).append(CLOSE_SPAN);
        }

        if (clazz.getInterfaces().length > 0) {
            sb.append(clazz.isInterface() ? " extends " : " implements ");
            for (int i = 0; i < clazz.getInterfaces().length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(OPEN_TYPE)
                        .append(clazz.getInterfaces()[i].getSimpleName()).append(CLOSE_SPAN);
            }
        }

        sb.append("</h2>");
        sb.append("<ul>");
        sb.append(buildFields(clazz));
        sb.append(buildConstructors(clazz));
        sb.append(buildMethods(clazz));
        sb.append("</ul>");
        sb.append("</div>");
        return sb.toString();
    }

    private static String resolveClassType(Class<?> clazz) {
        if (clazz.isEnum()) return "enum";
        if (clazz.isInterface()) return "";
        if (clazz.isRecord()) return "record";
        return "class";
    }

    private static String buildFields(Class<?> clazz) {
        StringBuilder sb = new StringBuilder();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getName().startsWith("$")) continue;
            sb.append("<li>")
                    .append(OPEN_MODIFIER).append(Modifier.toString(field.getModifiers())).append(CLOSE_SPAN).append(" ")
                    .append(OPEN_TYPE).append(field.getType().getSimpleName()).append(CLOSE_SPAN).append(" ")
                    .append(OPEN_STRONG).append(field.getName()).append(CLOSE_STRONG)
                    .append(CLOSE_LI);
        }
        return sb.toString();
    }

    private static String buildConstructors(Class<?> clazz) {
        if (clazz.isEnum()) return "";
        StringBuilder sb = new StringBuilder();
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            sb.append("<li>")
                    .append(OPEN_MODIFIER).append(Modifier.toString(constructor.getModifiers())).append(CLOSE_SPAN).append(" ")
                    .append(OPEN_STRONG).append(clazz.getSimpleName()).append(CLOSE_STRONG).append("(");
            Parameter[] params = constructor.getParameters();
            for (int i = 0; i < params.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(OPEN_TYPE).append(params[i].getType().getSimpleName()).append(CLOSE_SPAN).append(" ")
                        .append(params[i].getName());
            }
            sb.append(")").append(CLOSE_LI);
        }
        return sb.toString();
    }

    private static String buildMethods(Class<?> clazz) {
        StringBuilder sb = new StringBuilder();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isSynthetic() || isAutoGeneratedMethod(method, clazz)) continue;
            sb.append("<li>")
                    .append(OPEN_MODIFIER).append(Modifier.toString(method.getModifiers())).append(CLOSE_SPAN).append(" ")
                    .append(OPEN_TYPE).append(method.getReturnType().getSimpleName()).append(CLOSE_SPAN).append(" ")
                    .append(OPEN_STRONG).append(method.getName()).append(CLOSE_STRONG).append("(");
            Parameter[] params = method.getParameters();
            for (int i = 0; i < params.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(OPEN_TYPE).append(params[i].getType().getSimpleName()).append(CLOSE_SPAN).append(" ")
                        .append(params[i].getName());
            }
            sb.append(")").append(CLOSE_LI);
        }
        return sb.toString();
    }

    private static boolean isAutoGeneratedMethod(Method method, Class<?> clazz) {
        if (clazz.isEnum() && (method.getName().equals("values")
                || method.getName().equals("valueOf"))) return true;
        return clazz.isRecord() && (method.getName().equals("equals")
                || method.getName().equals("hashCode")
                || method.getName().equals("toString"));
    }

    private static void saveDocumentation(String html) {
        try {
            Files.createDirectories(DOC_FILE.getParent());
            try (PrintWriter writer = new PrintWriter(DOC_FILE.toFile())) {
                writer.write(html);
                logger.info("Documentation saved to: " + DOC_FILE);
            }
        } catch (IOException e) {
            logger.severe("Failed to save documentation: " + e.getMessage());
        }
    }
}