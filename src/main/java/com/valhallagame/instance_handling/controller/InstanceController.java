package com.valhallagame.instance_handling.controller;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.valhallagame.instance_handling.mesos.ValhallaMesosSchedulerClient;
import com.valhallagame.instance_handling.messages.InstanceAdd;
import com.valhallagame.instance_handling.messages.InstanceParameter;
import com.valhallagame.instance_handling.model.Task;
import com.valhallagame.instance_handling.repository.TaskRepository;
import com.valhallagame.instance_handling.utils.JS;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Controller
@RequestMapping(path = "/v1/instance-resource")
@Api(value = "InstanceQueueHandling")
public class InstanceController {

	@Autowired
	private TaskRepository taskRepository;
	
	@Autowired
	private ValhallaMesosSchedulerClient mesosClient;
	
	@RequestMapping(path = "/queue-instance", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Queues an instance.")
	@ResponseBody
	public Response start(InstanceAdd instanceAdd) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd HH:mm");
		String date = sdf.format(new Date());

		if (instanceAdd.getTaskId() == null || instanceAdd.getTaskId().equals("")) {
			String taskId = instanceAdd.getInstanceId() + " v:" + instanceAdd.getVersion() + " s:" + instanceAdd
					.getPersistentServerUrl() + " l:" + instanceAdd.getLevel() + " t:" + date;
			instanceAdd.setTaskId(taskId);
		}

		mesosClient.queueInstance(instanceAdd);
		
		Task task = new Task(instanceAdd.getTaskId(), instanceAdd.getInstanceId(), null);
		taskRepository.save(task);
		return JS.message(Status.OK, "Instance queued");
	}
	
	@RequestMapping(path = "/kill-instance", method = RequestMethod.POST)
	@ApiOperation(value = "Kills an instance without any checks. Pure killing!")
	@ResponseBody
	public Response killInstance(InstanceParameter instanceParameter) {
		Task task = taskRepository.findByInstanceId(instanceParameter.getInstanceId());
		mesosClient.kill(task.getTaskId());
		return JS.message(Status.OK, "Scheduled for killing");
	}
	
}
