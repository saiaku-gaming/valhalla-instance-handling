package com.valhallagame.instance_handling.controller;

import com.valhallagame.instance_handling.messages.InstanceAdd;
import com.valhallagame.instance_handling.messages.InstanceParameter;
import com.valhallagame.instance_handling.utils.JS;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
@RequestMapping(path = "/v1/instance-resource")
@Api(value = "InstanceQueueHandling")
public class InstanceController {

    @RequestMapping(path = "/queue-instance", method = RequestMethod.POST)
    @ApiOperation(value = "Queues an instance.")
    @ResponseBody
    public ResponseEntity<JS.JsonMessage> start(@RequestBody InstanceAdd instanceAdd) throws IOException {
        return JS.message(HttpStatus.OK, "Instance queued for creation");
    }

    @RequestMapping(path = "/kill-instance", method = RequestMethod.POST)
    @ApiOperation(value = "Kills an instance without any checks. Pure killing!")
    @ResponseBody
    public ResponseEntity<JS.JsonMessage> killInstance(@RequestBody InstanceParameter instanceParameter) {
        return null;
    }
}
