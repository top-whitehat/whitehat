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

import static top.whitehat.dns.DnsServer.*;

import java.net.InetAddress;

import top.whitehat.NetUtil;
import top.whitehat.dns.DnsServer.DnsRecord;

/**
 * DnsSession.
 * 
 * A DnsSession object contains a DnSRequest object, and a DnsResponse object.
 */
public class DnsSession {

	public static InetAddress RESULT_OK = NetUtil.toInetAddress("1.1.1.1", null);
	
	public static InetAddress RESULT_FAIL = NetUtil.toInetAddress("0.0.0.0", null);
	
	public static InetAddress RESULT_BYEBYE = NetUtil.toInetAddress("88.88.88.88", null);

	private DnsServer server;

	private DnsRequest request;

	private DnsResponse response;
	
	public DnsSession(DnsServer server, DnsPacket pkt) {
		this.server = server;
		this.request = new DnsRequest(pkt);
		this.response = new DnsResponse(this);
	}

	/** Get DnsServer object */
	public DnsServer getServer() {
		return server;
	}

	/** Get DnsRequest object */
	public DnsRequest getRequest() {
		return request;
	}

	/** Get DnsResponse object */
	public DnsResponse getResponse() {
		return response;
	}

	/** Get filters */
	public DnsFilters getFilters() {
		return server.getFilters();
	}
	
	/** Send error response to the client */
	public void responseError(DnsError errorCode) {
		response.sendError(errorCode);
	}

	/** send error response to the client */
	public void responseError() {
		response.sendError();
	}

	/** Send response by the filter mode */
	public void responseFilter(int filterMode) {
		response.sendFilterResult(filterMode);
	}

	/** Send response */
	public void response(DnsPacket msg) {
		response.send(msg.getBytes());
	}

	/** Send response of A query */
	public void responseA(InetAddress addr, long ttl) {
		if (addr == null)
			throw new RuntimeException("InetAddress is null");

		DnsPacket req = request.getDnsPacket();
		DnsPacket msg = req.responseA(req.queryName(), addr, ttl);
		response(msg);
	}

	/** Send response of A query */
	public void responseA(InetAddress addr) {
		responseA(addr, DnsPacket.DEFAULT_TTL);
	}
	
	/** Send response of A query */
	public void responseA(String ip, long ttl) {
		InetAddress addr = NetUtil.toInetAddress(ip, null);
		
		if (addr != null) { 
			responseA(addr, ttl);
		} else {
			throw new RuntimeException("invalid ip");
		}
	}
	
	/** Send response of A query */
	public void responseA(String ip) {
		responseA(ip, DnsPacket.DEFAULT_TTL);
	}
	
	/** Send response of PTR query */
	public void responsePTR(String ptrName) {
		DnsPacket msg = request.getDnsPacket().responsePTR(request.getDnsPacket().queryName(), ptrName, DnsPacket.DEFAULT_TTL);
		response(msg);
	}

	/** Send this request to trace server */
	public boolean trace() {
		// send this request to TraceServer
		InetAddress address = getFilters().traceServer.getAddress();		 
		if (address != null) {
			server.send(address, DNS.PORT, request.getDnsPacket().getBytes());
			return true;
		}

		return false;
	}

	/** Analysis the domain name, and return filter mode */
	public int getFilterMode(String domainName) {
		return server.getFilters().getFilterMode(domainName);
	}

	/** Find record in local machine
	 * 
	 * @return return true if find and processed. otherwise return false;
	 */
	public boolean findLocal() {
		DnsPacket req = request.getDnsPacket();
		DnsQueryType type = DnsQueryType.of(req.queryType());
		
		boolean isV6 = false;
		
		switch(type) {
		case AAAA:
			isV6 = true;
		case A:
			// find records
			DnsRecord r = findRecord(req.queryName(), isV6);
			
			// if found
			if (r != null) {
				server.debug("from local", req.queryName(), "=", r.address);
				if (r.address.isAnyLocalAddress()) {
					responseError(DnsError.UNKNOWN);
				} else {
					responseA(r.address, r.ttl);
				}
				return true;
			}
			break;
		default:
			
		}

		return false;
	}

	/** Find DNS record of specified domain name
	 * 
	 * @param domainName  The domain name to find
	 * @param isV6        Indicate whether find IPv6 address
	 * 
	 * @return return DNS record object is found, return null if not found.
	 */
	public DnsRecord findRecord(String domainName, boolean isV6) {
		return server.getRecords().get(domainName, isV6);
	}

	/** Bind specified domain name to specified IP address.
	 * 
	 * @param domainName the domain name
	 * @param ip         the ip address
	 * @param ttl        Time To Live in seconds
	 */
	public void setRecord(String domain, InetAddress ip, int ttl) {
		server.getRecords().set(domain, ip, ttl);
	}

	/** Get an argument of specified index from arguments array
	 * 
	 * @param args   The arguments array
	 * @param index  The index of the argument
	 * @param toLower Indicate whether convert the argument to lowercased
	 * @return String
	 */
	private String getArg(String[] args, int index, boolean toLower) {
		String value = index < args.length ? args[index] : "";
		value = toLower ? value.toLowerCase().trim() : value;
		return value;
	}

	/** Do the command */
	private boolean doCommand() {
		String[] args = request.getDnsPacket().queryName().split("=");

		String cmd = getArg(args, 0, true);
		switch (cmd) {
		case "command":
			return execCommand(args);
		case "upperdns":
		case "warning":
		case "trace":
			return execSetting(args, false);
		case "local":
		case "debug":
		case "ns":
			return execSetting(args, true);
		default:
			// 形如: domain=xxx, 动态设置一个域名
			return execDomain(args);
		}
	}

	/** Execute the command, such as: comand=XXX */
	private boolean execCommand(String[] args) {
		String arg = getArg(args, 1, true);

		switch (arg) {
		case "stop": // stop DNS server
			if (request.isFromLocal()) { // the request is from local
				responseA(DnsSession.RESULT_BYEBYE);
				server.stop();
			}
			break;
		default: // unknown
			responseError(DnsError.NOT_IMPLEMENT);
			break;
		}

		return true;
	}

	/***
	 * Execute setting command, such as: Keyword=XXX
	 * 
	 * @param args          command line arguments
	 * @param mustFromLocal Indicate whether need run from local
	 * @return
	 */
	private boolean execSetting(String[] args, boolean mustFromLocal) {
		if (!canExcecute())
			return false;

		if (mustFromLocal) {
			if (!request.isFromLocal()) {
				responseA(DnsSession.RESULT_FAIL);
				server.debug("command not from local machine");
				return true;
			}
		}

		String cmd = getArg(args, 0, true);
		String arg = getArg(args, 1, true);

		if (arg.length() == 0 || arg.equals("?")) {
			InetAddress ip = DnsSession.RESULT_FAIL;
			switch (cmd) {
			case "warning":
				ip = getFilters().warningServer.getAddress();
				if (ip == null)
					ip = DnsSession.RESULT_FAIL;
				break;
			case "trace":
				ip = getFilters().traceServer.getAddress();
				if (ip == null)
					ip = DnsSession.RESULT_FAIL;
				break;
			case "ns":
				ip = server.nsServer.getAddress();
				if (ip == null)
					ip = DnsSession.RESULT_FAIL;
				break;
			case "local":
				boolean ret = server.getLocalOnly();
				ip = NetUtil.toInetAddress("0.0.0." + (ret ? "1" : "0"), RESULT_FAIL);
				break;
			case "upperdns":
				ip = server.getUpperDnsAddress();
				break;
			case "debug":
				int num = server.getDebug();
				ip = NetUtil.toInetAddress("0.0.0." + String.valueOf(num), RESULT_FAIL);
				break;
			default:
				break;
			}
			responseA(ip);
			server.debug("get " + cmd, ip);
		} else {
			boolean ret = false;
			switch (cmd) {
			case "warning":
				ret = getFilters().warningServer.setName(arg);
				break;
			case "trace":
				ret = getFilters().traceServer.setName(arg);
				break;
			case "local":
				if (NetUtil.isBoolean(arg))
					ret = server.setLocalOnly(Boolean.valueOf(arg));
				break;
			case "upperdns":
				ret = server.setUpperDnsServer(arg);
				break;
			case "debug":
				if (NetUtil.isInteger(arg))
					ret = server.setDebug(Integer.valueOf(arg));
				break;
			case "ns":
				String nsIp = getArg(args, 2, true); 
				if (NetUtil.isIpV4(nsIp)) {
					server.setNsName(arg, nsIp);
					ret = true;
				}
			default:
				break;
			}

			server.debug("set", cmd, "to", arg, ret ? "OK" : "FAIL");
			responseA(ret ? DnsSession.RESULT_OK : DnsSession.RESULT_FAIL);
		}

		return true;

	}

	/** Execute domain set name, the command is like: www.some.com=XXXX  */
	private boolean execDomain(String[] args) {
		String arg = getArg(args, 1, true);

		if (NetUtil.isIpV4(arg)) {
			// format:  www.some.com=127.0.0.1, means set the IP of the domain name
			setDomain(args);
			args[1] = "pass";
			setFilter(args);
			return true;

		} else if (arg.equals("?")) {
			// format:  www.some.com=?  means get the filter mode of the domain name
			String domain = getArg(args, 0, true);
			int filterMode = server.getFilters().get(domain);
			responseA("0.0.0." + String.valueOf(filterMode));
			return true;
			
		} else if (DnsFilters.isKeyword(arg)) {
			// format:  www.some.com=stop  means set the filter mode of the domain name
			return setFilter(args);

		} else {
			// send error response
			responseError(DnsError.REFUSED);
			return true;
		}

	}

	/**
	 * Dynamically set a domain
	 * 
	 * @param args  command arguments
	 * 
	 * @return true
	 */
	private boolean setDomain(String[] args) {
		String domain = getArg(args, 0, true);
		String ip = getArg(args, 1, true);
		String sttl = getArg(args, 2, true);

		int ttl = DnsPacket.DEFAULT_TTL;
		ttl = NetUtil.toInt(sttl, 0);

		// set a record
		server.debug("set record", domain, "=", ip, "ttl=", ttl);
		InetAddress address = NetUtil.toInetAddress(ip, null);
		if (address != null) {
			setRecord(domain, address, ttl);
			// send response for A
			DnsPacket msg = request.getDnsPacket().responseA(domain, address, ttl);
			response(msg);
			return true;
		} else {
			DnsPacket msg = request.getDnsPacket().responseA(domain, DnsSession.RESULT_FAIL, ttl);
			response(msg);
			return false;
		}

	}

	/** Set a filter */
	private boolean setFilter(String[] args) {
		String domain = getArg(args, 0, true);
		String action = getArg(args, 1, true);
		int filterMode = DnsFilters.getValue(action);

		if (canExcecute()) {
			server.getFilters().set(domain, filterMode); // 设置过滤
			server.debug("set filter", domain, "=", filterMode);
			responseA(RESULT_OK);
		} else {
			responseError(DnsError.REFUSED);
		}

		return true;
	}

	/** Judge whether current user has the right to execute command */
	private boolean canExcecute() {
		if (server.getLocalOnly())
			return request.isFromLocal();
		else
			return true;
	}

	/** Run */
	public void run() {
		// if the request is PTR query, send the name of this server as response
		if (request.getDnsPacket().queryType() == DnsQueryType.PTR.getValue()) {
			if (request.getDnsPacket().queryName().equals(server.nsServer.getPtrName())) {
				responsePTR(server.nsServer.getName());
				return;
			}
		}

		// if the query does not need filter(A, AAAA, MX, HTTPS query)
		if (!request.needFilter()) {
			// send error response to the client
			responseError();
			return;
		}

		try {
			// get message of current request 
			DnsPacket msg = request.getDnsPacket();
			server.debug("receive query name=", msg.queryName(), "queryType=", msg.queryTypeName());

			// if the request is a command, do it
			if (request.isCommand()) {
				server.debug("receive command", msg.queryName());
				doCommand();
				return;
			}

			// if the request need filter, then do filter
			if (request.needFilter()) {
				int mode = getFilterMode(msg.queryName());
				if (mode == DnsFilters.TRACE) {
					trace();
				} else if (mode > DnsFilters.TRACE) {
					server.debug(msg.queryName(), "filter mode=", mode);
					responseFilter(mode);
					return;
				}
			}

			// firstly, find local records
			if (findLocal()) {
				// successfully find in local, return
				return;
			}

			//  transfer the query to upper DNS server
			if (!server.upperDns.query(request.getDnsPacket())) {
				// when transfer failed, send error response
				responseError();
			}

		} catch (Exception e) {
			// do nothing
//			e.printStackTrace();
		}

	}
}
