package io.kellermann.services.youtube;

import io.kellermann.model.gd.Status;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Component
public class StaticTemplateService {

    private TemplateEngine templateEngine;

    private TemplateEngine getTemplateEngine() {
        if (null == templateEngine) {
            templateEngine = new TemplateEngine();
            StringTemplateResolver templateResolver = new StringTemplateResolver();
            templateResolver.setTemplateMode(TemplateMode.HTML);
            templateEngine.setTemplateResolver(templateResolver);
        }
        return templateEngine;
    }


    public String getTemplateFromMap(String htmlContent, Map<String, Object> dynamicAttibutesMap) {
        templateEngine = getTemplateEngine();
        String template = null;
        final Context ctx = new Context(Locale.GERMAN);
        if (!CollectionUtils.isEmpty(dynamicAttibutesMap)) {
            dynamicAttibutesMap.forEach((k, v) -> ctx.setVariable(k, v));
        }
        if (null != templateEngine) {
            template = templateEngine.process(htmlContent, ctx);
        }
        return template;
    }


    @PostConstruct
    public void test() {
        Map<String, Object> test = new HashMap<>();
        test.put("test", new Status("Test", 2));
        System.err.println(
                getTemplateFromMap("<th:block th:inline=\"text\">ABC [[${test.getMessage()}]]\n testg</th:block>", test));
    }
}
