package com.rewe.digital.calendar.api;

import org.apache.commons.io.FileUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

@RestController
public class IndexController {

    @RequestMapping("/")
    public String index(final HttpServletResponse response) throws IOException {
        final ClassLoader classLoader = getClass().getClassLoader();
        final File file = new File(classLoader.getResource("public/index.htm").getFile());

        return FileUtils.readFileToString(file);
    }
}