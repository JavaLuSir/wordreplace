package org.javalu.docgen.controller;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.ObjectFactory;
import org.javalu.docgen.util.DateUtil;
import org.javalu.docgen.util.Docx4jUtils;
import org.javalu.docgen.util.PropertyUtil;
import org.springframework.boot.jackson.JsonObjectSerializer;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class DocController {


    @ResponseBody
    @RequestMapping("/read")
    public String read(){
        Map<String, String> mp = PropertyUtil.readmap();
        return  JSONObject.toJSONString(mp);
    }

    @ResponseBody
    @RequestMapping("/save")
    public String save(@RequestParam Map<String,String> data){
        Map<String,String> rst = new HashMap<>();
        String t = data.get("t");
        String cvalue = data.get("c");
        String nvalue = data.get("n");

        data.remove("t");
        data.remove("c");
        data.remove("n");
        data.put("c"+t,cvalue);
        data.put("n"+t,nvalue);
        try {
            PropertyUtil.savemap(data);
        } catch (Exception e) {
            e.printStackTrace();
            rst.put("code","-1");
            return JSONObject.toJSONString(rst);
        }

        rst.put("code","0");
        return JSONObject.toJSONString(rst);
    }

    @ResponseBody
    @RequestMapping("/savedate")
    public String savedate(@RequestParam Map<String,String> data){

        try {
            data.forEach((k,v)->{
                if(!k.equals("gs")){
                    data.put(k,String.format("%02d",Integer.parseInt(v)));
                }
            });
            PropertyUtil.savemap(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<String,String> rst = new HashMap<>();
        rst.put("code","0");
        return JSONObject.toJSONString(rst);
    }

    @RequestMapping("/clear")
    public void clear(HttpServletResponse response) throws IOException {
        String verion= StringUtils.substring(UUID.randomUUID().toString().replace("-",""),28);
        String sourcefile = System.getProperty("user.dir") + "/template/data.properties";
        String targetfile = System.getProperty("user.dir") + "/template/"+ DateUtil.getUTCTimeStr()+"_"+verion+".properties";
        FileInputStream fins = new FileInputStream(sourcefile);
        FileOutputStream fous = new FileOutputStream(targetfile);
        try {
            FileCopyUtils.copy(fins,fous);
            PropertyUtil.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.sendRedirect("/");
    }

    @ResponseBody
    @RequestMapping("/download")
    public void download(HttpServletRequest request, HttpServletResponse response){
        String inputfilepath = System.getProperty("user.dir") + "/template/report.docx";
        FileInputStream fins = null;
        try {
            fins = new FileInputStream(inputfilepath);
            Map<String, String> mp = PropertyUtil.readmap();
            //处理换行
            mp.forEach((k,v)->{
                mp.put(k,Docx4jUtils.newlineToBreakHack(v));
            });
            StringBuffer filename = new StringBuffer("信息通信工作一周要情");
            filename.append(DateUtil.getYear()+"0"+mp.get("sm")+mp.get("sd")+"-0"+mp.get("em")+mp.get("ed"));

            Docx4jUtils.downloadDocUseDoc4j(fins,mp,response,filename.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            try {
                fins.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    public static void writedoc(Map<String,String> mappings) throws Exception {

            // Exclude context init from timing
            ObjectFactory foo = Context.getWmlObjectFactory();

            // Input docx has variables in it: ${colour}, ${icecream}
            String inputfilepath = System.getProperty("user.dir") + "/template/report.docx";

            boolean save = true;
            String outputfilepath = System.getProperty("user.dir") + "/OUT_VariableReplace.docx";

            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new File(inputfilepath));

            MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
            Docx4jUtils.cleanDocumentPart(documentPart);
            long start = System.currentTimeMillis();
        System.out.println("maps:"+mappings);

        documentPart.variableReplace(mappings);

            long end = System.currentTimeMillis();
            long total = end - start;
            System.out.println("Time: " + total);

            // Save it
            if (save) {
                SaveToZipFile saver = new SaveToZipFile(wordMLPackage);
                saver.save(outputfilepath);
            } else {
                System.out.println(XmlUtils.marshaltoString(documentPart.getJaxbElement(), true,
                        true));
            }
    }


}
