package com.yang.apt.lib.processor;

import com.yang.apt.lib.annotation.BindView;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

public class BindViewProcessor extends AbstractProcessor {


    private Messager mMessager;

    //init 方法最新被执行
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mMessager = processingEnv.getMessager();
        mMessager.printMessage(Diagnostic.Kind.NOTE, "init");
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new HashSet<>();
        types.add(BindView.class.getCanonicalName());
        return types;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        print("process");

        if (annotations.isEmpty()){
            return false;
        }

        //获取被注解的对象集合
        Set<? extends Element> elementsAnnotatedSet = roundEnv.getElementsAnnotatedWith(BindView.class);

        Map<String, List<VariableElement>> map = new HashMap<>();
        for (Element element : elementsAnnotatedSet){

//        TypeElement//类
//        ExecutableElement//方法
//        VariableElement//属性

            VariableElement variableElement = (VariableElement) element;
            //获取类名
            String clazzName = variableElement.getEnclosingElement().getSimpleName().toString();
            List<VariableElement> variableElements = map.computeIfAbsent(clazzName, k -> new ArrayList<>());
            variableElements.add(variableElement);
        }

        print(map.toString());

        if (!map.isEmpty()){
            Iterator<String> iterator = map.keySet().iterator();
            while (iterator.hasNext()){
                String next = iterator.next();
                List<VariableElement> variableElements = map.get(next);

                TypeElement typeElement = (TypeElement) variableElements.get(0).getEnclosingElement();

                PackageElement packageElement = processingEnv.getElementUtils().getPackageOf(typeElement);

                String packageName = packageElement.toString();
                try {
                    JavaFileObject file = processingEnv.getFiler().createSourceFile(packageName + "." + next + "_ViewBinding");
                    Writer writer = file.openWriter();
                    writer.write("package " + packageName + ";\n\n");
                    writer.write("public class " + next + "_ViewBinding {\n\n}");
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    private void print(String log){
        mMessager.printMessage(Diagnostic.Kind.NOTE, "BindViewProcessor " + log);
    }
}
