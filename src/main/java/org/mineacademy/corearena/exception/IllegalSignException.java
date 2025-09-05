package org.mineacademy.corearena.exception;

import lombok.Getter;

public class IllegalSignException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	@Getter
	private final String problem;

	public IllegalSignException(String message) {
		this.problem = message;
	}
}
