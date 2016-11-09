package com.valhallagame.instance_handling;

import static com.mesosphere.mesos.rx.java.protobuf.ProtoUtils.protoToString;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class State<FwId, TaskId, TaskState> {
	private static final Logger LOGGER = LoggerFactory.getLogger(State.class);

	private final double cpusPerTask;
	private final double memMbPerTask;

	private final String resourceRole;

	private final Map<TaskId, TaskState> taskStates;

	private final AtomicInteger offerCounter = new AtomicInteger();

	private final AtomicInteger totalTaskCounter = new AtomicInteger();

	private final FwId fwId;

	public State(final FwId fwId, final String resourceRole, final double cpusPerTask, final double memMbPerTask) {
		this.fwId = fwId;
		this.resourceRole = resourceRole;
		this.cpusPerTask = cpusPerTask;
		this.memMbPerTask = memMbPerTask;
		this.taskStates = new ConcurrentHashMap<>();
	}

	public FwId getFwId() {
		return fwId;
	}

	public double getCpusPerTask() {
		return cpusPerTask;
	}

	public double getMemMbPerTask() {
		return memMbPerTask;
	}

	public String getResourceRole() {
		return resourceRole;
	}

	public AtomicInteger getOfferCounter() {
		return offerCounter;
	}

	public AtomicInteger getTotalTaskCounter() {
		return totalTaskCounter;
	}

	public void put(final TaskId key, final TaskState value) {
		LOGGER.debug("put(key : {}, value : {})", protoToString(key), value);
		taskStates.put(key, value);
	}
}