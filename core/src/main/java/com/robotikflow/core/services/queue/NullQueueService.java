package com.robotikflow.core.services.queue;

import com.robotikflow.core.models.queue.Message;

public final class NullQueueService implements QueueService 
{
	@Override
	public void enviar(
		final Message mensagem) 
		throws Exception {
	}

	@Override
	public void enviar(
		final String exchange, 
		final String destino, 
		final Message mensagem) 
		throws Exception {
	}
	
	@Override
	public Message receber() {
		return null;
	}

	@Override
	public Message receber(
		final long timeoutEmMs) 
		throws Exception {
		return null;
	}
}
