package org.javalu.docgen.util;
 
 
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
 
/**
 * 关于文件操作的工具类
 *
 * @author kaizen
 * @date 2018-10-23 17:21:36
 */
public final class Docx4jUtils {
 
    private static final Logger logger = LoggerFactory.getLogger(Docx4jUtils.class);
 
    /**
     * 替换变量并下载word文档
     *
     * @param inputStream
     * @param map
     * @param response
     * @param fileName
     */
    public static void downloadDocUseDoc4j(InputStream inputStream, Map<String, String> map,
                                          HttpServletResponse response, String fileName) {
 
        try {
            // 设置响应头
            fileName = URLEncoder.encode(fileName, "UTF-8");
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".docx");
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
 
            OutputStream outs  = response.getOutputStream();
            Docx4jUtils.replaceDocUseDoc4j(inputStream,map,outs);
 
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
 
    /**
     * 替换变量并输出word文档
     * @param inputStream
     * @param map
     * @param outputStream
     */
    public static void replaceDocUseDoc4j(InputStream inputStream, Map<String, String> map,
                                          OutputStream outputStream) {
        try {
            WordprocessingMLPackage doc = WordprocessingMLPackage.load(inputStream);
            MainDocumentPart mainDocumentPart = doc.getMainDocumentPart();
            if (null != map && !map.isEmpty()) {
                // 将${}里的内容结构层次替换为一层
                Docx4jUtils .cleanDocumentPart(mainDocumentPart);
                // 替换文本内容
                mainDocumentPart.variableReplace(map);
            }
 
            // 输出word文件
            doc.save(outputStream);
            outputStream.flush();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
 
 
    /**
     * cleanDocumentPart
     *
     * @param documentPart
     */
    public static boolean cleanDocumentPart(MainDocumentPart documentPart) throws Exception {
        if (documentPart == null) {
            return false;
        }
        Document document = documentPart.getContents();
        String wmlTemplate =
                XmlUtils.marshaltoString(document, true, false, Context.jc);
        document = (Document) XmlUtils.unwrap(DocxVariableClearUtils.doCleanDocumentPart(wmlTemplate, Context.jc));
        documentPart.setContents(document);
        return true;
    }
 
    /**
     * 清扫 docx4j 模板变量字符,通常以${variable}形式
     * <p>
     * XXX: 主要在上传模板时处理一下, 后续
     *
     * @author liliang
     * @since 2018-11-07
     */
    private static class DocxVariableClearUtils {
 
 
        /**
         * 去任意XML标签
         */
        private static final Pattern XML_PATTERN = Pattern.compile("<[^>]*>");
 
        private DocxVariableClearUtils() {
        }
 
        /**
         * start符号
         */
        private static final char PREFIX = '$';
 
        /**
         * 中包含
         */
        private static final char LEFT_BRACE = '{';
 
        /**
         * 结尾
         */
        private static final char RIGHT_BRACE = '}';
 
        /**
         * 未开始
         */
        private static final int NONE_START = -1;
 
        /**
         * 未开始
         */
        private static final int NONE_START_INDEX = -1;
 
        /**
         * 开始
         */
        private static final int PREFIX_STATUS = 1;
 
        /**
         * 左括号
         */
        private static final int LEFT_BRACE_STATUS = 2;
 
        /**
         * 右括号
         */
        private static final int RIGHT_BRACE_STATUS = 3;
 
 
        /**
         * doCleanDocumentPart
         *
         * @param wmlTemplate
         * @param jc
         * @return
         * @throws JAXBException
         */
        private static Object doCleanDocumentPart(String wmlTemplate, JAXBContext jc) throws JAXBException {
            // 进入变量块位置
            int curStatus = NONE_START;
            // 开始位置
            int keyStartIndex = NONE_START_INDEX;
            // 当前位置
            int curIndex = 0;
            char[] textCharacters = wmlTemplate.toCharArray();
            StringBuilder documentBuilder = new StringBuilder(textCharacters.length);
            documentBuilder.append(textCharacters);
            // 新文档
            StringBuilder newDocumentBuilder = new StringBuilder(textCharacters.length);
            // 最后一次写位置
            int lastWriteIndex = 0;
            for (char c : textCharacters) {
                switch (c) {
                    case PREFIX:
                        // TODO 不管其何状态直接修改指针,这也意味着变量名称里面不能有PREFIX
                        keyStartIndex = curIndex;
                        curStatus = PREFIX_STATUS;
                        break;
                    case LEFT_BRACE:
                        if (curStatus == PREFIX_STATUS) {
                            curStatus = LEFT_BRACE_STATUS;
                        }
                        break;
                    case RIGHT_BRACE:
                        if (curStatus == LEFT_BRACE_STATUS) {
                            // 接上之前的字符
                            newDocumentBuilder.append(documentBuilder.substring(lastWriteIndex, keyStartIndex));
                            // 结束位置
                            int keyEndIndex = curIndex + 1;
                            // 替换
                            String rawKey = documentBuilder.substring(keyStartIndex, keyEndIndex);
                            // 干掉多余标签
                            String mappingKey = XML_PATTERN.matcher(rawKey).replaceAll("");
                            if (!mappingKey.equals(rawKey)) {
                                char[] rawKeyChars = rawKey.toCharArray();
                                // 保留原格式
                                StringBuilder rawStringBuilder = new StringBuilder(rawKey.length());
                                // 去掉变量引用字符
                                for (char rawChar : rawKeyChars) {
                                    if (rawChar == PREFIX || rawChar == LEFT_BRACE || rawChar == RIGHT_BRACE) {
                                        continue;
                                    }
                                    rawStringBuilder.append(rawChar);
                                }
                                // FIXME 要求变量连在一起
                                String variable = mappingKey.substring(2, mappingKey.length() - 1);
                                int variableStart = rawStringBuilder.indexOf(variable);
                                if (variableStart > 0) {
                                    rawStringBuilder = rawStringBuilder.replace(variableStart, variableStart + variable.length(), mappingKey);
                                }
                                newDocumentBuilder.append(rawStringBuilder.toString());
                            } else {
                                newDocumentBuilder.append(mappingKey);
                            }
                            lastWriteIndex = keyEndIndex;
 
                            curStatus = NONE_START;
                            keyStartIndex = NONE_START_INDEX;
                        }
                    default:
                        break;
                }
                curIndex++;
            }
            // 余部
            if (lastWriteIndex < documentBuilder.length()) {
                newDocumentBuilder.append(documentBuilder.substring(lastWriteIndex));
            }
            return XmlUtils.unmarshalString(newDocumentBuilder.toString(), jc);
        }
 
    }


    /**
     * Hack to convert a new line character into w:br.
     * If you need this sort of thing, consider using
     * OpenDoPE content control data binding instead.
     *
     * @param r
     * @return
     */
    public static String newlineToBreakHack(String r) {

        StringTokenizer st = new StringTokenizer(r, "\n\r\f"); // tokenize on the newline character, the carriage-return character, and the form-feed character
        StringBuilder sb = new StringBuilder();

        boolean firsttoken = true;
        while (st.hasMoreTokens()) {
            String line = (String) st.nextToken();
            if (firsttoken) {
                firsttoken = false;
            } else {
                sb.append("</w:t><w:br/><w:t>");
            }
            sb.append(line);
        }
        return sb.toString();
    }
}