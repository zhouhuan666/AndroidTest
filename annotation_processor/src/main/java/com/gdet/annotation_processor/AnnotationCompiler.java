package com.gdet.annotation_processor;

import com.gdet.annotations.BindView;
import com.google.auto.service.AutoService;

import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

/**
 * @author JNCHOU
 * 版本：1.0
 * 创建日期：2023-11-18
 * 描述：
 * 注解处理程序
 */
@AutoService(Process.class)
public class AnnotationCompiler extends AbstractProcessor {


    //支持版本
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    //能用来处理哪些注释


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new HashSet<String>();
        types.add(BindView.class.getCanonicalName());
        return types;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

//        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "huan----" + annotations);
        Set<? extends Element> elementsAnnotationedWith = roundEnv.getElementsAnnotatedWith(BindView.class);

        Map<String, List<VariableElement>> map = new HashMap<>();
        for (Element element : elementsAnnotationedWith) {
            VariableElement variableElement = (VariableElement) element;
            String activityName = variableElement.getEnclosingElement().getSimpleName().toString();
            Class aClass = variableElement.getEnclosingElement().getClass();
            List<VariableElement> variableElements = map.get(activityName);
            if (variableElements == null) {
                variableElements = new ArrayList<>();
                map.put(activityName, variableElements);
            }
            variableElements.add(variableElement);
        }


        if (map.size() > 0) {
            Writer writer = null;
        }


        return false;
    }
}
