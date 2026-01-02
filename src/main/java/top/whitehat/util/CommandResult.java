/*
 * Copyright 2026 The WhiteHat Project
 *
 * The WhiteHat Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package top.whitehat.util;

/**
 * CommandResult is the return value of the run() method of CommandLine
 */
public class CommandResult {

	/** return value */
	private int returnValue = -1;

	/** output text */
	private String output = "";
	
	/** Constructor */
	public CommandResult(int returnValue, String output) {
		this.returnValue = returnValue;
		this.output = output;
	}
	
	/** get return value of the command line */
	public int getReturnValue() {
		return returnValue;
	}
	
	/** get output string of the command line */
	public String getOutput() {
		return output;
	}

	/** use output as Text object */
	public Text text() {
		return new Text(output);
	}
	
	/** use output as Text object, and filter rows by expression */
	public Text text(String expr) {
		return new Text(output).filter(expr);
	}
	
	
	/** use output as TextRow object */
	public TextRow rows() {
		return new TextRow(output);
	}
	
	/** use output as TextRow object, and filter rows by expression */
	public TextRow rows(String expr) {
		return new TextRow(output).filter(expr);
	}

	public String toString() {
		return output;
	}

}
