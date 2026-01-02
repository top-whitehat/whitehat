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
package top.whitehat.dns;

/**
 * DNS response error codes
 * https://www.rfc-editor.org/rfc/rfc1035#section-4.1.1
 * 
 */
public enum DnsError {
	
	/** DNS response error codes: no error */
	NO_ERROR(0),

	/** DNS response error codes: error format */
	FORMAT(1),

	/** DNS response error codes: service fail */
	SERVICE_FAIL(2),

	/** DNS response error codes: error name */
	NAME(3),

	/** DNS response error codes: not implement */
	NOT_IMPLEMENT(4),

	/** DNS response error codes: refused */
	REFUSED(5),
	
	/** UNKNOWN error */
	UNKNOWN(0xFF);
	

	private int value = 0;

	DnsError(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return this.value;
	}
	
	public static DnsError of(int value) {
		for (DnsError q : DnsError.values())
			if (q.getValue() == value)
				return q;
		
		return UNKNOWN;
	}
	
}
