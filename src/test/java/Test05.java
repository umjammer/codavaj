/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.codavaj.process.docparser.DocParser;
import org.codavaj.type.Type;
import org.codavaj.type.TypeFactory;

import com.google.common.collect.Streams;

import spoon.Launcher;
import spoon.SpoonAPI;
import spoon.javadoc.internal.Pair;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtMethod;
import spoon.support.reflect.declaration.CtClassImpl;


/**
 * Test04. using spoon (replace comment, refactoring parameter name: failed)
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/05/10 umjammer initial version <br>
 */
public class Test05 {

    /** */
    public TypeFactory analyze(String javadocdir, List<String> externalLinks) throws Exception {
        DocParser dp = new DocParser();
        dp.setJavadocClassName("quicktime\\.[A-Z]\\w+$");
        dp.setJavadocDirName(javadocdir);
        dp.setExternalLinks(externalLinks);
        dp.addProgressListener(System.err::println);

        return dp.process();
    }

    /**
     * 
     * @param args
     */
    public static void main(String[] args) throws Exception {

        String javadocDir = args[0];
        String externalLink = args[1];
        String sourceDir = args[2];
        String outputDir = args[3];

        List<String> el = new ArrayList<>();
        el.add(externalLink);

        Test05 app = new Test05();
        TypeFactory tf = app.analyze(javadocDir, el);

        for (Type type : tf.getTypes()) {

            Path sourcePath = Paths.get(sourceDir, type.getSourceFilename());
            if (!Files.exists(sourcePath)) {
//                WriterUtils.print(type, Files.newBufferedWriter(source));
System.err.println("SK: " + sourcePath);
                continue;
            }

            SpoonAPI api = new Launcher();
            api.getEnvironment().setAutoImports(true);
            api.getEnvironment().setNoClasspath(true);
            api.getEnvironment().setComplianceLevel(8);
            api.addInputResource(sourcePath.toString());
            api.addProcessor(new AbstractProcessor<CtMethod<?>>() {
                @Override
                public void process(CtMethod<?> element) {
                    if (CtClassImpl.class.isInstance(element.getParent())) {
                        type.getType(CtClassImpl.class.cast(element.getParent()).getSimpleName()).ifPresent(t -> {
System.err.println("CM: METHOD: " + getSignatureString(element));
                            t.getMethod(getSignatureString(element)).ifPresent(m -> {
                                Streams.zip(m.getParameterList().stream(), element.getParameters().stream(),
                                    (a, b) -> new Pair<>(a, b)
                                ).filter(p -> {
                                    return !p.a.getName().equals(p.b.getSimpleName());
                                }).forEach(p -> {
System.err.println("RN: " + "PARAM: " + p.b.getSimpleName() + " -> " + p.a.getName() + " \t\t/ " + getSignatureString(element));
                                    p.b.setSimpleName(p.a.getName()); // TODO this is not refactoring
                                });
                            });
                        });
                    }
                }

                /** */
                String getSignatureString(CtMethod<?> n) {
                    StringBuilder sb = new StringBuilder(n.getSimpleName());
                    sb.append("(");
                    n.getParameters().forEach(p -> {
                        sb.append(Type.getSignatureString(tf.getFullyQualifiedName(p.getType().toString())));
                    });
                    sb.append(")");
                    sb.append(Type.getSignatureString(tf.getFullyQualifiedName(n.getType().toString())));
//System.err.println("SG: "+ sb.toString());
                    return sb.toString();
                }
            });
            api.addProcessor(new AbstractProcessor<CtConstructor<?>>() {
                @Override
                public void process(CtConstructor<?> element) {
                    if (CtClassImpl.class.isInstance(element.getParent())) {
                        type.getType(CtClassImpl.class.cast(element.getParent()).getSimpleName()).ifPresent(t -> {
System.err.println("CM: CONSTRUCTOR: " + getSignatureString(element));
                            t.getMethod(getSignatureString(element)).ifPresent(m -> {
                                Streams.zip(m.getParameterList().stream(), element.getParameters().stream(),
                                    (a, b) -> new Pair<>(a, b)
                                ).filter(p -> {
                                    return !p.a.getName().equals(p.b.getSimpleName());
                                }).forEach(p -> {
System.err.println("RN: " + "PARAM: " + p.b.getSimpleName() + " -> " + p.a.getName() + " \t\t/ " + getSignatureString(element));
                                    p.b.setSimpleName(p.a.getName()); // TODO this is not refactoring
                                });
                            });
                        });
                    }
                }

                /** */
                String getSignatureString(CtConstructor<?> n) {
                    StringBuilder sb = new StringBuilder(CtClassImpl.class.cast(n.getParent()).getSimpleName());
                    sb.append("(");
                    n.getParameters().forEach(p -> {
                        sb.append(Type.getSignatureString(tf.getFullyQualifiedName(p.getType().toString())));
                    });
                    sb.append(")");
//System.err.println("sg: "+ sb.toString());
                    return sb.toString();
                }
            });

            Path result = Paths.get(outputDir, type.getSourceFilename());
System.err.println("WR: "+ result);

//            api.prettyprint();
            api.setSourceOutputDirectory(outputDir);
            api.run();
        }
    }
}

/* */
