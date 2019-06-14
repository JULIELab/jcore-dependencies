package cc.mallet.util;

import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

import java.io.File;

public class InstanceListPrinter {

	public static void main(String[] args) throws Exception {
		InstanceList instances = InstanceList.load(new File(args[0]));

		for (Instance instance: instances) {
			System.out.println("Name: " + instance.getName());
			System.out.println("Target: " + instance.getTarget());
			System.out.println("Data: " + instance.getData());
		}
	}

}