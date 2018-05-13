package com.higgs.trust.rs.custom.controller.outter.v1;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系統操作類
 *
 * @author lingchao
 * @create 2018年03月14日20:40
 */
@RestController
public class SystemController {
    @RequestMapping(value = "/status.html", method = RequestMethod.GET)
    String status_html() {
        return "imok";
    }
}
