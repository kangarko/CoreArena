package org.mineacademy.corearena.exception;

/**
 *
 * Methods that throw this are usually handled so:
 *
 * SimpleImpl --> FeatureImpl --> SpecificImpl
 *
 * Represents that a method in the pipeline does not
 * need to be handled further with "super." or similar.
 */
public final class EventHandledException extends RuntimeException {
	private static final long serialVersionUID = 1L;
}
