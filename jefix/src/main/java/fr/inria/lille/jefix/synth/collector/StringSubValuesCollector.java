/*
 * Copyright (C) 2013 INRIA
 *
 * This software is governed by the CeCILL-C License under French law and
 * abiding by the rules of distribution of free software. You can use, modify
 * and/or redistribute the software under the terms of the CeCILL-C license as
 * circulated by CEA, CNRS and INRIA at http://www.cecill.info.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the CeCILL-C License for more details.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.inria.lille.jefix.synth.collector;


/**
 * @author Favio D. DeMarco
 *
 */
final class StringSubValuesCollector implements SubValuesCollector {

	/**
	 * @see fr.inria.lille.jefix.synth.collector.SubValuesCollector#addSubValues(java.lang.String, java.lang.Object)
	 */
	@Override
	public void addSubValues(final String name, final Object value) {
		String string = (String) value;
		ValuesCollector.add(name + ".length()", string.length());
		ValuesCollector.add(name + ".isEmpty()", string.isEmpty());
	}
}
