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
package top.whitehat.pcap;

/** PcapException is an exception that has an error code and a message.​  */
public class PcapException extends RuntimeException {
        
    private static final long serialVersionUID = 81428080856993457L;
    
	protected int errorCode = 0;
    	
	public PcapException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public PcapException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public int getErrorCode() {
    	return errorCode;
    }
}
