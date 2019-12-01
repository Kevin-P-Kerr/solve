package com.lang;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;

public class ProofService implements Callable<ProofResult> {
	private final ProofTask taskA;
	private final ProofTask taskB; // prove the negative of a
	private final ExecutorService exec;

	public ProofService(ProofTask a, ProofTask b, ExecutorService exec) {
		this.taskA = a;
		this.taskB = b;
		this.exec = exec;
	}

	@Override
	public ProofResult call() throws Exception {
		CompletionService<ProofResult> cs = new ExecutorCompletionService<>(exec);
		try {
		cs.submit(taskA);
		cs.submit(taskB);
		return cs.take().get();
		} catch (InterruptedException e) {
			cs.c
		}
	}

}
