package com.yang.apt.lib.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.yang.apt.lib.annotation.BindView;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class BindViewProcessor extends AbstractProcessor {


    private Messager mMessager;

    //init 方法最先被执行
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        //获取打印
        mMessager = processingEnv.getMessager();
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
        print("process：开始");

        //此方法可能会进入三次，第一次annotations不为空，后面为空
        if (annotations.isEmpty()){
            return false;
        }

        //获取被注解的对象集合
        Set<? extends Element> elementsAnnotatedSet = roundEnv.getElementsAnnotatedWith(BindView.class);

        Map<String, List<VariableElement>> map = new HashMap<>();
        for (Element element : elementsAnnotatedSet){

//        TypeElement//类，注解使用到类上，就是得到类元素
//        ExecutableElement//方法，注解使用到方法上，就是得到可执行元素
//        VariableElement//变量，注解使用到成员变量上，就是得到变量元素

            VariableElement variableElement = (VariableElement) element;
            //获取类名
            String clazzName = variableElement.getEnclosingElement().getSimpleName().toString();
            List<VariableElement> variableElements = map.computeIfAbsent(clazzName, k -> new ArrayList<>());
            variableElements.add(variableElement);
        }

        if (!map.isEmpty()){
            for (String clazzName : map.keySet()) {
                List<VariableElement> variableElements = map.get(clazzName);

                //生成  **_ViewBinding.java文件
                //createViewBindingFile(clazzName, variableElements);
                createViewBindingFileByJavaPoet(clazzName, variableElements);
            }
        }

        print("process：结束");

        //返回false，此方法还会被进入，因为生成了代码，生成了代码中可能还有其他注解，需要再生成代码
        return false;
    }


    /**
     * 使用JavaPoet生成  **_ViewBinding.java文件
     *
     * 如：com.yang.apt.demo.MainActivity_ViewBinding.java
     */
    private void createViewBindingFileByJavaPoet(String clazzName, List<VariableElement> variableElements) {
        //获取类元素
        TypeElement typeElement = (TypeElement) variableElements.get(0).getEnclosingElement();
        PackageElement packageElement = processingEnv.getElementUtils().getPackageOf(typeElement);
        //获取包名
        String packageName = packageElement.toString();
        try {
            //构建方法：public void bind(MainActivity target)
            MethodSpec.Builder bindBuilder = MethodSpec.methodBuilder("bind")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(TypeName.get(typeElement.asType()), "target")
                    .returns(void.class);

            for (VariableElement variableElement : variableElements) {
                //获取变量名
                String variableName = variableElement.getSimpleName().toString();
                //获取变量的注解值
                int id = variableElement.getAnnotation(BindView.class).value();
                //获取变量的类型
                TypeMirror typeMirror = variableElement.asType();

                //给方法添加语句：target.textView = (android.widget.TextView)target.findViewById(2131165338);
                bindBuilder.addStatement("target.$L = ($L)target.findViewById($L)", variableName, typeMirror.toString(), id);
            }

            //构建类： MainActivity_ViewBinding
            TypeSpec typeSpec = TypeSpec.classBuilder(clazzName + "_ViewBinding")
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(bindBuilder.build())
                    .build();

            //构建Java文件
            JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();

            //写入.java文件
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成  **_ViewBinding.java文件
     *
     * 如：com.yang.apt.demo.MainActivity_ViewBinding.java
     */
    private void createViewBindingFile(String clazzName, List<VariableElement> variableElements) {
        TypeElement typeElement = (TypeElement) variableElements.get(0).getEnclosingElement();

        PackageElement packageElement = processingEnv.getElementUtils().getPackageOf(typeElement);

        //获取包名
        String packageName = packageElement.toString();
        try {
            JavaFileObject file = processingEnv.getFiler().createSourceFile(packageName + "." + clazzName + "_ViewBinding");
            Writer writer = file.openWriter();
            writer.write("package " + packageName + ";");
            writer.write("\n\npublic class " + clazzName + "_ViewBinding {");

            writer.write("\n\n\tpublic void bind(" + clazzName + " target) {");

            for (VariableElement variableElement : variableElements) {
                //获取变量名
                String variableName = variableElement.getSimpleName().toString();
                //获取变量的注解值
                int id = variableElement.getAnnotation(BindView.class).value();
                //获取变量的类型
                TypeMirror typeMirror = variableElement.asType();
                writer.write("\n\t\ttarget." + variableName + " = (" + typeMirror.toString() + ")target.findViewById(" + id + ");");
            }

            writer.write("\n\t}");
            writer.write("\n}");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void print(String log){
        mMessager.printMessage(Diagnostic.Kind.NOTE, "BindViewProcessor " + log);
    }
}
