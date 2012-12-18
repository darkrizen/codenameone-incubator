package ca.weblite.codename1.build.ios;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;


/**
 *
 * @author shannah
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class NativeAnnotationProcessor extends AbstractProcessor {
    
    Hashtable xmlvmTypes = new Hashtable();
    Hashtable xmlvmTransformations = new Hashtable();
    
    public NativeAnnotationProcessor(){
        Object[] xtypes = new Object[]{
          String.class, "JAVA_OBJECT",
          String[].class, "JAVA_OBJECT",
          int.class, "JAVA_INT",
          short.class, "JAVA_SHORT",
          byte.class, "JAVA_BYTE",
          char.class, "JAVA_CHAR",
          long.class, "JAVA_LONG",
          float.class, "JAVA_FLOAT",
          double.class, "JAVA_DOUBLE",
          int[].class, "JAVA_OBJECT",
          float[].class, "JAVA_OBJECT",
          double[].class, "JAVA_OBJECT",
          short[].class, "JAVA_OBJECT",
          byte[].class, "JAVA_OBJECT",
          boolean.class, "JAVA_BOOLEAN",
          boolean[].class, "JAVA_OBJECT",
          long.class, "JAVA_LONG",
          long[].class, "JAVA_OBJECT",
          void.class, "void"
        };
        
        for ( int i=0; i<xtypes.length; i+=2){
            xmlvmTypes.put(xtypes[i], xtypes[i+1]);
        }
        
        Object[] xtransformations = new Object[]{
          String.class, "fromNSString",
          
          int[].class, "nsDataToIntArray",
          float[].class, "nsDataToFloatArray",
          double[].class, "nsDataToDoubleArray",
          short[].class, "nsDataToShortArray",
          byte[].class, "nsDataToByteArray",
          boolean[].class, "nsDataToBooleanArray",
          long[].class, "nsDataToLongArray"
        };
        
        for (int i=0; i<xtransformations.length; i+=2){
            xmlvmTransformations.put(xtransformations[i], xtransformations[i+1]);
        }
        
        
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Properties props = new Properties();
        props.put("resource.loader", "class");         
        props.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(props);
        //Properties properties = new Properties();
	//properties.setProperty("file.resource.loader.path","d:/templates");
        //Velocity.init("velocity.properties");
        //this.processingEnv.getMessager().printMessage(
        //                    Diagnostic.Kind.NOTE,
        //                    "Starting processing");
        Set<? extends Element> elements = roundEnv.getRootElements();
        for ( Element el : elements){
            //this.processingEnv.getMessager().printMessage(
            //                Diagnostic.Kind.NOTE,
            //                "Doing root element "+ el.toString());
            process(el);
        }
        return true;
    }

    private File getTemplate(String template) throws IOException{
        InputStream is = null;
        FileOutputStream fos = null;
        File out = null;
        try {
            is = NativeAnnotationProcessor.class.getResourceAsStream(template);
            out = File.createTempFile("NativeImplTemplate", "vm");
            //out.deleteOnExit();
            fos = new FileOutputStream(out);
            byte[] buffer = new byte[4096];
            int readBytes;
            while ( (readBytes = is.read(buffer)) > 0){
                fos.write(buffer, 0, readBytes);
            }
        } finally {
            try {
                if ( is != null ) is.close();
            } catch(Exception ex){}
            try {
                if ( fos != null ) fos.close();
            } catch(Exception ex){}
        }
        
        return out;
        
    }
    
    protected String nsValue(Class type, String name){
        
        if ( String.class.equals(type)){
            return "toNSString("+name+")";
        } else if ( type.isArray() ){
            return "arrayToData("+name+")";
        } else {
            return name;
        }
    }
    
    protected String xmlvmType(Class type){
        this.processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.NOTE,
                            "Getting xmlvmType for class "+type);
        if ( type == null ) return "void";
        String name = (String)xmlvmTypes.get(type);
        this.processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.NOTE,
                            "XMLVM type result "+name);
        if ( name == null ){
            throw new RuntimeException("Failed to find XMLVM type for "+type);
        }
        return name;
    }
    
    protected String mangleParameters(Class[] params){
        StringBuilder paramStr = new StringBuilder();
        for ( int i=0; i<params.length; i++){
            paramStr.append(mangleParameter(params[i]));
            if ( i < params.length-1){
                paramStr.append("_");
            }
        }
        return paramStr.toString();
    }
    
    protected String mangleParameter(Class param){
        return param.getCanonicalName().replace(".", "_");
    }
    
    protected String xmlvmReturnTypeTransformation(Class returnType){
        String out = (String)xmlvmTransformations.get(returnType);
        if ( out == null ) out = "";
        return out;
    }
    
    private boolean process(Element el){
        
        
        
        if ( el.getKind().isInterface()){
            this.processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.NOTE,
                            "Is interface "+el);
            Types te = this.processingEnv.getTypeUtils();
            TypeElement tel = (TypeElement)el;
            Element parentElement = te.asElement(tel.getSuperclass());
            Class cls;
            try {
                cls = Class.forName(tel.getQualifiedName().toString());
            } catch (Exception ex){
                this.processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.NOTE,
                            "Failed to get class "+tel);
                return false;
            }
            Class spr = cls.getSuperclass();
            System.out.println("Superclass is "+spr);
            Class interfaces[] = cls.getInterfaces();
            boolean isNativeInterface = false;
            for ( int i=0; i<interfaces.length; i++){
                if ( "com.codename1.system.NativeInterface".equals(interfaces[i].getCanonicalName())){
                    isNativeInterface = true;
                    break;
                }
            }
            if ( !isNativeInterface ){
                this.processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.NOTE,
                            "Is not native interface "+el);
                return false;
            }
            try {
                
                VelocityContext c = new VelocityContext();
                String fullClassName = tel.getQualifiedName().toString();
                String packageName = fullClassName.substring(0, fullClassName.lastIndexOf("."));
                c.put("packageName", packageName);
                c.put("className", tel.getSimpleName()+"Impl");
                c.put("stubClassName", tel.getSimpleName()+"Stub");
                c.put("implClassName", tel.getSimpleName()+"ImplCodenameOne");
                c.put("mangledClassNameImpl", tel.getQualifiedName().toString().replace(".", "_")+"Impl");
                c.put("implements", el.asType().toString());
                Vector<Hashtable> methodContexts = new Vector<Hashtable>();
                Method[] methods = cls.getDeclaredMethods();
                for ( int i=0; i<methods.length; i++){
                    Hashtable mc = new Hashtable();
                    mc.put("methodName", methods[i].getName());
                    mc.put("returnType", methods[i].getReturnType().getCanonicalName());
                    mc.put("returnTypeXMLVM", xmlvmType(methods[i].getReturnType()));
                    mc.put("returnTypeTransformation", xmlvmReturnTypeTransformation(methods[i].getReturnType()));
                    Vector<Hashtable> parameters = new Vector<Hashtable>();
                    Class[] params = methods[i].getParameterTypes();
                    mc.put("mangledParamTypes", mangleParameters(params));
                    this.processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.NOTE,
                            methods[i]+" has "+params.length+" parameters ");
                    for (int j=0; j<params.length; j++){
                        Hashtable pc = new Hashtable();
                        pc.put("parameterType", params[j].getCanonicalName());
                        pc.put("parameterName", "param"+j);
                        pc.put("nsValue", nsValue(params[j], "param"+j));
                        pc.put("parameterTypeXMLVM", xmlvmType(params[j]));
                        parameters.add(pc);
                    }
                    mc.put("parameters", parameters);
                    methodContexts.add(mc);
                    
                }
                c.put("methods", methodContexts);
                
                
                
                Template template = null;
                try {
                    //template = Velocity.getTemplate(getTemplate("NativeImpl.vm").getCanonicalPath());
                    template = Velocity.getTemplate("ca/weblite/codename1/build/ios/NativeImpl.vm");
                } catch ( Exception ex){
                    this.processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.NOTE,
                            "Failed to load velocity template from jar "+el);
                }
                        
                
                // At this point we've found a native interface.
                // We need to create the implementation file
                
                FileObject out_file = processingEnv.getFiler()
                        .createSourceFile(el.asType().toString()+"ImplCodenameOne", el);
                
                Writer w = out_file.openWriter();
                template.merge(c, w);
                //w.write("package ca.weblite.codename1.io;\nclass NativeCookieStorageImplsadflkjdlkjadfklsfjsda{}");
                
                w.flush();
                w.close();
                
                try {
                    //template = Velocity.getTemplate(getTemplate("NativeImpl.vm").getCanonicalPath());
                    template = Velocity.getTemplate("ca/weblite/codename1/build/ios/Stub.vm");
                } catch ( Exception ex){
                    this.processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.NOTE,
                            "Failed to load velocity template from jar "+el);
                }
                        
                
                // At this point we've found a native interface.
                // We need to create the implementation file
                
                out_file = processingEnv.getFiler()
                        .createSourceFile(el.asType().toString()+"Stub", el);
                
                w = out_file.openWriter();
                template.merge(c, w);
                //w.write("package ca.weblite.codename1.io;\nclass NativeCookieStorageImplsadflkjdlkjadfklsfjsda{}");
                
                w.flush();
                w.close();
                
                try {
                    //template = Velocity.getTemplate(getTemplate("NativeImpl.vm").getCanonicalPath());
                    template = Velocity.getTemplate("ca/weblite/codename1/build/ios/NativeC.vm");
                } catch ( Exception ex){
                    this.processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.NOTE,
                            "Failed to load velocity template from jar "+el);
                }
                        
                
                // At this point we've found a native interface.
                // We need to create the implementation file
                
                out_file = processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, "", "native_"+c.get("mangledClassNameImpl")+"CodenameOne.m", el);
                        
                
                w = out_file.openWriter();
                template.merge(c, w);
                //w.write("package ca.weblite.codename1.io;\nclass NativeCookieStorageImplsadflkjdlkjadfklsfjsda{}");
                
                w.flush();
                w.close();
                
                
            } catch (IOException ex) {
                Logger.getLogger(NativeAnnotationProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        
        List<? extends Element> children = el.getEnclosedElements();
        for ( Element child : children ){
            process(child);
        }
        
        
        
        return true;
        
    }
    
    
    
}
