package com.valhallagame.instance_handling.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.valhallagame.instance_handling.mesos.ValhallaMesosSchedulerClient;
import com.valhallagame.instance_handling.messages.InstanceAdd;
import com.valhallagame.instance_handling.messages.InstanceParameter;
import com.valhallagame.instance_handling.model.Task;
import com.valhallagame.instance_handling.service.TaskService;
import com.valhallagame.instance_handling.utils.JS;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Controller
@RequestMapping(path = "/v1/instance-resource")
@Api(value = "InstanceQueueHandling")
public class InstanceController {

	@Autowired
	private TaskService taskService;
	
	@Autowired
	private ValhallaMesosSchedulerClient mesosClient;
	
	@RequestMapping(path = "/queue-instance", method = RequestMethod.POST)
	@ApiOperation(value = "Queues an instance.")
	@ResponseBody
	public ResponseEntity<?> start(@RequestBody InstanceAdd instanceAdd) throws IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd HH:mm");
		String date = sdf.format(new Date());
		if (instanceAdd.getTaskId() == null || instanceAdd.getTaskId().equals("")) {
			String taskId = instanceAdd.getInstanceId() + " v:" + instanceAdd.getVersion() + " s:" + instanceAdd
					.getPersistentServerUrl() + " l:" + instanceAdd.getLevel() + " t:" + date;
			instanceAdd.setTaskId(taskId);
		}
		mesosClient.queueInstance(instanceAdd);
		Task task = new Task(instanceAdd.getTaskId(), instanceAdd.getInstanceId(), null);
		taskService.save(task);
		
		return JS.message(HttpStatus.OK, "Instance queued for creation");
	}
	
	@RequestMapping(path = "/kill-instance", method = RequestMethod.POST)
	@ApiOperation(value = "Kills an instance without any checks. Pure killing!")
	@ResponseBody
	public ResponseEntity<?> killInstance(@RequestBody InstanceParameter instanceParameter) {
		Optional<Task> taskOpt = taskService.getTask(instanceParameter.getInstanceId());
		if(taskOpt.isPresent()) {
			mesosClient.kill(taskOpt.get().getTaskId());
			return JS.message(HttpStatus.OK, "Scheduled for killing");
		} else {
			return JS.message(HttpStatus.NOT_FOUND, "Could not find task with instance id: " + instanceParameter.getInstanceId());
		}
	}
	
}
