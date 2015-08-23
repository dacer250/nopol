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
package fr.inria.lille.repair.nopol;

import static java.lang.String.format;

import java.io.File;
import java.net.URL;
import java.util.*;

import com.gzoltar.core.instr.testing.TestResult;
import fr.inria.lille.commons.synthesis.ConstraintBasedSynthesis;
import fr.inria.lille.commons.synthesis.operator.Operator;
import fr.inria.lille.repair.Main;
import fr.inria.lille.repair.common.config.Config;
import fr.inria.lille.repair.common.patch.Patch;
import fr.inria.lille.repair.nopol.synth.DefaultSynthesizer;
import fr.inria.lille.repair.nopol.synth.SynthesizerFactory;
import fr.inria.lille.repair.common.synth.StatementType;
import fr.inria.lille.spirals.repair.vm.VMAcquirer;
import org.apache.commons.io.FileUtils;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtType;

public class NoPolLauncher {
	
	public static void main(String[] args) {
		String filePath = "misc/nopol-example/src/";
		String binFolder = "misc/nopol-example/bin/";
		String junitJar = "misc/nopol-example/junit-4.11.jar";
		String classpath = binFolder + File.pathSeparatorChar + junitJar;
		String type = args[0];
		String solverName = args[1];
		String solverPath = args[2];
		System.out.println(format("args:\n\t%s\n\t%s\n\t%s\n\t%s", filePath, classpath, solverName, solverPath));
		Main.main(new String[] {"repair", type, "angelic", filePath, classpath, solverName, solverPath});
	}

	public static List<Patch> launch(File[] sourceFile, URL[] classpath, StatementType type, String[] args) {
		long executionTime = System.currentTimeMillis();
		NoPol nopol = new NoPol(sourceFile, classpath, type);
		List<Patch> patches = null;
		try {
			if (args.length > 0) {
				patches = nopol.build(args);
			} else {
				patches = nopol.build();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			executionTime = System.currentTimeMillis() - executionTime;
			displayResult(nopol, patches, executionTime);
		}
		return patches;
	}

	public static ArrayList<Integer> nbFailingTestExecution = new ArrayList<>();
	public static ArrayList<Integer> nbPassedTestExecution = new ArrayList<>();

	private static void displayResult(NoPol nopol, List<Patch> patches, long executionTime){
		System.out.println("----INFORMATION----");
		List<CtType<?>> allClasses = nopol.getSpooner().spoonFactory().Class().getAll();
		int nbMethod = 0;
		for (int i = 0; i < allClasses.size(); i++) {
			CtType<?> ctSimpleType = allClasses.get(i);
			if(ctSimpleType instanceof CtClass) {
				Set methods = ((CtClass) ctSimpleType).getMethods();
				nbMethod+= methods.size();
			}
		}
		System.out.println("Nb classes : " + allClasses.size());
		System.out.println("Nb methods : " + nbMethod);
		System.out.println("Nb unit tests : " + nopol.getgZoltar().getGzoltar().getTestResults().size());
		System.out.println("Nb Statements Analyzed : "+SynthesizerFactory.getNbStatementsAnalysed());
		System.out.println("Nb Statements with Angelic Value Found : "+DefaultSynthesizer.getNbStatementsWithAngelicValue());
        System.out.println("Nb inputs in SMT : "+DefaultSynthesizer.getDataSize());
		System.out.println("Nb SMT level: "+ ConstraintBasedSynthesis.level);
		System.out.println("Nb SMT components: [" + ConstraintBasedSynthesis.operators.size() + "] " + ConstraintBasedSynthesis.operators);
		Iterator<Operator<?>> iterator = ConstraintBasedSynthesis.operators.iterator();
		Map<Class, Integer> mapType = new HashMap<>();
		while (iterator.hasNext()) {
			Operator<?> next = iterator.next();
			if(!mapType.containsKey(next.type())) {
				mapType.put(next.type(), 1);
			} else {
				mapType.put(next.type(), mapType.get(next.type()) + 1);
			}
		}
		for (Iterator<Class> patchIterator = mapType.keySet().iterator(); patchIterator.hasNext(); ) {
			Class next = patchIterator.next();
			System.out.println("                  "  + next + ": " + mapType.get(next));
		}

		System.out.println("Nb variables in SMT : "+DefaultSynthesizer.getNbVariables());
		System.out.println("Nb run failing test  : " + nbFailingTestExecution);
		System.out.println("Nb run passing test : " + nbPassedTestExecution);
		System.out.println("Nopol Execution time : "+ executionTime +"ms");

		if (patches != null && ! patches.isEmpty() ){
			System.out.println("----PATCH FOUND----");
			for ( Patch patch : patches ){
				System.out.println(patch);
			}
		}
	}
}
