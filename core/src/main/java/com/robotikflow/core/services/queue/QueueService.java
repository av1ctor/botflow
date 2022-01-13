package com.robotikflow.core.services.queue;

import com.robotikflow.core.models.queue.Message;

public interface QueueService 
{
	void enviar(
		final Message mensagem) 
		throws Exception;

	void enviar(
		final String exchange, 
		final String destino, 
		final Message mensagem) 
		throws Exception;

	Message receber() 
		throws Exception;
	
	Message receber(
		final long timeoutEmMs) 
		throws Exception;
}