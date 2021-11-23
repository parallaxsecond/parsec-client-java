package org.parallaxsecond.parsec.jce.provider;

import org.parallaxsecond.parsec.client.core.BasicClient;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * Interface to retrieve a parsec client. We will need to choose if we use:
 *
 * <ol>
 *   <li>a globally shared client
 *   <li>a client per provider
 *   <li>a client per provider and thread
 *   <li>a new client per call
 * </ol>
 */
public interface ParsecClientAccessor extends Supplier<BasicClient>, Serializable {}
