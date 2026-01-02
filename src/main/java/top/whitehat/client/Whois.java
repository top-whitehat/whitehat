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
package top.whitehat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import top.whitehat.util.JSON;
import top.whitehat.util.Text;


/**
 * Use Whois protocol (RFC 3912) to get information of a domain
 */
public class Whois {

	private static final int WHOIS_PORT = 43;
	private static final String DEFAULT_SERVER = "whois.verisign-grs.com";
	private static final String ORG_SERVER = "whois.pir.org";
	private static final String CNNIC_SERVER = "whois.cnnic.cn";

	/** return whois server of specified domain */
	protected static String getWhoisServer(String domain) {
		String tld = domain.substring(domain.lastIndexOf('.') + 1).toLowerCase();
		switch (tld) {
		case "cn":
			return CNNIC_SERVER;
		case "com":
		case "net":
		case "edu":
			return DEFAULT_SERVER;
		case "org":
			return ORG_SERVER;
		default:
			return getwhoisServers().getOrDefault(tld, DEFAULT_SERVER);
		}
	}

	/** make Whois query */
	protected static String whoisQuery(String domain) {
		String server = getWhoisServer(domain);

		try (Socket socket = new Socket(server, WHOIS_PORT);
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

			socket.setSoTimeout(2000);

			// send request : domain + CRLF
			out.println(domain);
			out.flush();

			// read response
			StringBuilder response = new StringBuilder();
			String line;
			while ((line = in.readLine()) != null) {
				response.append(line).append("\n");
			}
			return response.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/** Get information of specified domain */
	public static JSON of(String domain) {
		String str = whoisQuery(domain);

		// cut down the text after >>>
		if (str.indexOf(">>>") > 0)
			str = str.substring(0, str.indexOf(">>>"));

		Map<String, Object> map = Text.of(str).filter(":").split(":", 2) // convert to TextTable
				.setFieldNames("name", "value").toMap("name", "value");

		return JSON.parse(map); // convert to JSON
	}

	/** cache of whois servers */
	private static Map<String, String> whoisServers = null;

	/** get whois servers */
	private static Map<String, String> getwhoisServers() {
		if (whoisServers == null) {
			whoisServers = new HashMap<String, String>();
			whoisServers.put("com", "whois.verisign-grs.com");
			whoisServers.put("net", "whois.verisign-grs.com");
			whoisServers.put("org", "whois.pir.org");
			whoisServers.put("info", "whois.afilias.net");
			whoisServers.put("biz", "whois.nic.biz");
			whoisServers.put("name", "whois.nic.name");
			whoisServers.put("pro", "whois.registry.pro");
			whoisServers.put("mobi", "whois.dotmobiregistry.net");
			whoisServers.put("travel", "whois.nic.travel");
			whoisServers.put("museum", "whois.museum");
			whoisServers.put("aero", "whois.aero");
			whoisServers.put("coop", "whois.nic.coop");
			whoisServers.put("jobs", "whois.nic.jobs");
			whoisServers.put("cat", "whois.cat");
			whoisServers.put("tel", "whois.nic.tel");
			whoisServers.put("asia", "whois.nic.asia");
			whoisServers.put("xxx", "whois.nic.xxx");
			whoisServers.put("post", "whois.nic.post");
			whoisServers.put("app", "whois.nic.app");
			whoisServers.put("dev", "whois.nic.dev");
			whoisServers.put("io", "whois.nic.io");
			whoisServers.put("tech", "whois.nic.tech");
			whoisServers.put("online", "whois.nic.online");
			whoisServers.put("site", "whois.nic.site");
			whoisServers.put("store", "whois.nic.store");
			whoisServers.put("blog", "whois.nic.blog");
			whoisServers.put("cloud", "whois.nic.cloud");
			whoisServers.put("fun", "whois.nic.fun");
			whoisServers.put("xyz", "whois.nic.xyz");
			whoisServers.put("shop", "whois.nic.shop");
			whoisServers.put("live", "whois.nic.live");
			whoisServers.put("wiki", "whois.nic.wiki");
			whoisServers.put("design", "whois.nic.design");
			whoisServers.put("link", "whois.nic.link");
			whoisServers.put("click", "whois.nic.click");
			whoisServers.put("top", "whois.nic.top");
			whoisServers.put("vip", "whois.nic.vip");
			whoisServers.put("club", "whois.nic.club");
			whoisServers.put("fit", "whois.nic.fit");
			whoisServers.put("guru", "whois.nic.guru");
			whoisServers.put("zone", "whois.nic.zone");
			whoisServers.put("space", "whois.nic.space");
			whoisServers.put("host", "whois.nic.host");
			whoisServers.put("press", "whois.nic.press");
			whoisServers.put("email", "whois.nic.email");
			whoisServers.put("company", "whois.nic.company");
			whoisServers.put("group", "whois.nic.group");
			whoisServers.put("center", "whois.nic.center");
			whoisServers.put("global", "whois.nic.global");
			whoisServers.put("world", "whois.nic.world");
			whoisServers.put("today", "whois.nic.today");
			whoisServers.put("city", "whois.nic.city");
			whoisServers.put("academy", "whois.nic.academy");
			whoisServers.put("agency", "whois.nic.agency");
			whoisServers.put("bar", "whois.nic.bar");
			whoisServers.put("bike", "whois.nic.bike");
			whoisServers.put("bio", "whois.nic.bio");
			whoisServers.put("blue", "whois.nic.blue");
			whoisServers.put("build", "whois.nic.build");
			whoisServers.put("capital", "whois.nic.capital");
			whoisServers.put("cards", "whois.nic.cards");
			whoisServers.put("care", "whois.nic.care");
			whoisServers.put("cash", "whois.nic.cash");
			whoisServers.put("chat", "whois.nic.chat");
			whoisServers.put("city", "whois.nic.city");
			whoisServers.put("claims", "whois.nic.claims");
			whoisServers.put("cleaning", "whois.nic.cleaning");
			whoisServers.put("click", "whois.nic.click");
			whoisServers.put("clinic", "whois.nic.clinic");
			whoisServers.put("codes", "whois.nic.codes");
			whoisServers.put("community", "whois.nic.community");
			whoisServers.put("company", "whois.nic.company");
			whoisServers.put("computer", "whois.nic.computer");
			whoisServers.put("construction", "whois.nic.construction");
			whoisServers.put("consulting", "whois.nic.consulting");
			whoisServers.put("contractors", "whois.nic.contractors");
			whoisServers.put("cooking", "whois.nic.cooking");
			whoisServers.put("cool", "whois.nic.cool");
			whoisServers.put("country", "whois.nic.country");
			whoisServers.put("credit", "whois.nic.credit");
			whoisServers.put("cruises", "whois.nic.cruises");
			whoisServers.put("dance", "whois.nic.dance");
			whoisServers.put("dating", "whois.nic.dating");
			whoisServers.put("degree", "whois.nic.degree");
			whoisServers.put("democrat", "whois.nic.democrat");
			whoisServers.put("dental", "whois.nic.dental");
			whoisServers.put("diamonds", "whois.nic.diamonds");
			whoisServers.put("diet", "whois.nic.diet");
			whoisServers.put("digital", "whois.nic.digital");
			whoisServers.put("direct", "whois.nic.direct");
			whoisServers.put("directory", "whois.nic.directory");
			whoisServers.put("discount", "whois.nic.discount");
			whoisServers.put("doctor", "whois.nic.doctor");
			whoisServers.put("dog", "whois.nic.dog");
			whoisServers.put("domains", "whois.nic.domains");
			whoisServers.put("download", "whois.nic.download");
			whoisServers.put("earth", "whois.nic.earth");
			whoisServers.put("energy", "whois.nic.energy");
			whoisServers.put("engineer", "whois.nic.engineer");
			whoisServers.put("engineering", "whois.nic.engineering");
			whoisServers.put("estate", "whois.nic.estate");
			whoisServers.put("events", "whois.nic.events");
			whoisServers.put("exchange", "whois.nic.exchange");
			whoisServers.put("expert", "whois.nic.expert");
			whoisServers.put("exposed", "whois.nic.exposed");
			whoisServers.put("express", "whois.nic.express");
			whoisServers.put("fail", "whois.nic.fail");
			whoisServers.put("farm", "whois.nic.farm");
			whoisServers.put("finance", "whois.nic.finance");
			whoisServers.put("financial", "whois.nic.financial");
			whoisServers.put("fish", "whois.nic.fish");
			whoisServers.put("fitness", "whois.nic.fitness");
			whoisServers.put("flights", "whois.nic.flights");
			whoisServers.put("florist", "whois.nic.florist");
			whoisServers.put("flowers", "whois.nic.flowers");
			whoisServers.put("futbol", "whois.nic.futbol");
			whoisServers.put("gallery", "whois.nic.gallery");
			whoisServers.put("games", "whois.nic.games");
			whoisServers.put("gifts", "whois.nic.gifts");
			whoisServers.put("glass", "whois.nicklass");
			whoisServers.put("graphics", "whois.nic.graphics");
			whoisServers.put("gratis", "whois.nic.gratis");
			whoisServers.put("green", "whois.nic.green");
			whoisServers.put("gripe", "whois.nic.gripe");
			whoisServers.put("group", "whois.nic.group");
			whoisServers.put("guide", "whois.nic.guide");
			whoisServers.put("guitars", "whois.nic.guitars");
			whoisServers.put("guru", "whois.nic.guru");
			whoisServers.put("haus", "whois.nic.haus");
			whoisServers.put("health", "whois.nic.health");
			whoisServers.put("healthcare", "whois.nic.healthcare");
			whoisServers.put("help", "whois.nic.help");
			whoisServers.put("hiphop", "whois.nic.hiphop");
			whoisServers.put("hiv", "whois.nic.hiv");
			whoisServers.put("holdings", "whois.nic.holdings");
			whoisServers.put("holiday", "whois.nic.holiday");
			whoisServers.put("homes", "whois.nic.homes");
			whoisServers.put("horse", "whois.nic.horse");
			whoisServers.put("house", "whois.nic.house");
			whoisServers.put("immo", "whois.nic.immo");
			whoisServers.put("industries", "whois.nic.industries");
			whoisServers.put("info", "whois.afilias.net");
			whoisServers.put("ink", "whois.nic.ink");
			whoisServers.put("institute", "whois.nic.institute");
			whoisServers.put("insure", "whois.nic.insure");
			whoisServers.put("international", "whois.nic.international");
			whoisServers.put("investments", "whois.nic.investments");
			whoisServers.put("jetzt", "whois.nic.jetzt");
			whoisServers.put("jewelry", "whois.nic.jewelry");
			whoisServers.put("kim", "whois.nic.kim");
			whoisServers.put("kitchen", "whois.nic.kitchen");
			whoisServers.put("kiwi", "whois.nic.kiwi");
			whoisServers.put("kaufen", "whois.nic.kaufen");
			whoisServers.put("land", "whois.nic.land");
			whoisServers.put("lease", "whois.nic.lease");
			whoisServers.put("legal", "whois.nic.legal");
			whoisServers.put("life", "whois.nic.life");
			whoisServers.put("lighting", "whois.nic.lighting");
			whoisServers.put("limited", "whois.nic.limited");
			whoisServers.put("limo", "whois.nic.limo");
			whoisServers.put("link", "whois.nic.link");
			whoisServers.put("live", "whois.nic.live");
			whoisServers.put("llc", "whois.nic.llc");
			whoisServers.put("loan", "whois.nic.loan");
			whoisServers.put("loans", "whois.nic.loans");
			whoisServers.put("luxury", "whois.nic.luxury");
			whoisServers.put("management", "whois.nic.management");
			whoisServers.put("market", "whois.nic.market");
			whoisServers.put("marketing", "whois.nic.marketing");
			whoisServers.put("media", "whois.nic.media");
			whoisServers.put("men", "whois.nic.men");
			whoisServers.put("menu", "whois.nic.menu");
			whoisServers.put("miami", "whois.nic.miami");
			whoisServers.put("moda", "whois.nic.moda");
			whoisServers.put("money", "whois.nic.money");
			whoisServers.put("monster", "whois.nic.monster");
			whoisServers.put("mortgage", "whois.nic.mortgage");
			whoisServers.put("movie", "whois.nic.movie");
			whoisServers.put("network", "whois.nic.network");
			whoisServers.put("ninja", "whois.nic.ninja");
			whoisServers.put("online", "whois.nic.online");
			whoisServers.put("org", "whois.pir.org");
			whoisServers.put("partners", "whois.nic.partners");
			whoisServers.put("parts", "whois.nic.parts");
			whoisServers.put("photo", "whois.nic.photo");
			whoisServers.put("photography", "whois.nic.photography");
			whoisServers.put("photos", "whois.nic.photos");
			whoisServers.put("pics", "whois.nic.pics");
			whoisServers.put("pictures", "whois.nic.pictures");
			whoisServers.put("pizza", "whois.nic.pizza");
			whoisServers.put("place", "whois.nic.place");
			whoisServers.put("plumbing", "whois.nic.plumbing");
			whoisServers.put("plus", "whois.nic.plus");
			whoisServers.put("poker", "whois.nic.poker");
			whoisServers.put("porn", "whois.nic.porn");
			whoisServers.put("press", "whois.nic.press");
			whoisServers.put("pro", "whois.registry.pro");
			whoisServers.put("productions", "whois.nic.productions");
			whoisServers.put("properties", "whois.nic.properties");
			whoisServers.put("property", "whois.nic.property");
			whoisServers.put("pub", "whois.nic.pub");
			whoisServers.put("racing", "whois.nic.racing");
			whoisServers.put("recipes", "whois.nic.recipes");
			whoisServers.put("red", "whois.nic.red");
			whoisServers.put("rehab", "whois.nic.rehab");
			whoisServers.put("reise", "whois.nic.reise");
			whoisServers.put("reisen", "whois.nic.reisen");
			whoisServers.put("cn", "whois.cnnic.cn");
			whoisServers.put("uk", "whois.nic.uk");
			whoisServers.put("de", "whois.denic.de");
			whoisServers.put("fr", "whois.afnic.fr");
			whoisServers.put("jp", "whois.jprs.jp");
			whoisServers.put("kr", "whois.kr");
			whoisServers.put("cn", "whois.cnnic.cn");
			whoisServers.put("ru", "whois.tcinet.ru");
			whoisServers.put("br", "whois.registro.br");
			whoisServers.put("it", "whois.nic.it");
			whoisServers.put("es", "whois.nic.es");
			whoisServers.put("eu", "whois.eu");
			whoisServers.put("ca", "whois.cira.ca");
			whoisServers.put("au", "whois.auda.org.au");
			whoisServers.put("in", "whois.registry.in");
			whoisServers.put("mx", "whois.mx");
			whoisServers.put("nl", "whois.sidn.nl");
			whoisServers.put("se", "whois.iis.se");
			whoisServers.put("ch", "whois.nic.ch");
			whoisServers.put("at", "whois.nic.at");
			whoisServers.put("be", "whois.dns.be");
			whoisServers.put("pl", "whois.dns.pl");
			whoisServers.put("cz", "whois.nic.cz");
			whoisServers.put("gr", "whois.ics.forth.gr");
			whoisServers.put("co", "whois.nic.co");
			whoisServers.put("io", "whois.nic.io");
			whoisServers.put("me", "whois.nic.me");
			whoisServers.put("tv", "whois.nic.tv");
			whoisServers.put("cc", "whois.nic.cc");
			whoisServers.put("ws", "whois.nic.ws");
			whoisServers.put("bz", "whois.belizenic.bz");
			whoisServers.put("sc", "whois.nic.sc");
			whoisServers.put("vc", "whois.nic.vc");
			whoisServers.put("gg", "whois.gg");
			whoisServers.put("je", "whois.je");
			whoisServers.put("im", "whois.nic.im");
			whoisServers.put("li", "whois.nic.li");
			whoisServers.put("lu", "whois.dns.lu");
			whoisServers.put("pt", "whois.dns.pt");
			whoisServers.put("fi", "whois.fi");
			whoisServers.put("no", "whois.norid.no");
			whoisServers.put("dk", "whois.dk-hostmaster.dk");
			whoisServers.put("ie", "whois.iedr.ie");
			whoisServers.put("za", "whois.registry.net.za");
			whoisServers.put("ar", "whois.nic.ar");
			whoisServers.put("cl", "whois.nic.cl");
			whoisServers.put("pe", "whois.nic.pe");
			whoisServers.put("ve", "whois.nic.ve");
			whoisServers.put("co", "whois.nic.co");
			whoisServers.put("mx", "whois.mx");
			whoisServers.put("uy", "whois.nic.org.uy");
			whoisServers.put("py", "whois.nic.py");
			whoisServers.put("ec", "whois.nic.ec");
			whoisServers.put("gt", "whois.nic.gt");
			whoisServers.put("sv", "whois.nic.sv");
			whoisServers.put("hn", "whois.nic.hn");
			whoisServers.put("ni", "whois.nic.ni");
			whoisServers.put("pa", "whois.nic.pa");
			whoisServers.put("do", "whois.nic.do");
			whoisServers.put("jm", "whois.nic.jm");
			whoisServers.put("tt", "whois.nic.tt");
			whoisServers.put("bb", "whois.nic.bb");
			whoisServers.put("bm", "whois.nic.bm");
			whoisServers.put("bs", "whois.nic.bs");
			whoisServers.put("ky", "whois.nic.ky");
			whoisServers.put("vg", "whois.nic.vg");
			whoisServers.put("ai", "whois.nic.ai");
			whoisServers.put("aw", "whois.nic.aw");
			whoisServers.put("bm", "whois.nic.bm");
			whoisServers.put("bz", "whois.belizenic.bz");
			whoisServers.put("dm", "whois.nic.dm");
			whoisServers.put("gd", "whois.nic.gd");
			whoisServers.put("gl", "whois.nic.gl");
			whoisServers.put("gp", "whois.nic.gp");
			whoisServers.put("lc", "whois.nic.lc");
			whoisServers.put("mf", "whois.nic.mf");
		}
		return whoisServers;
	};

}
