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
package top.whitehat.tcp;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;


/**
 * Commonly used TCP ports <br>
 * 
 * <a href="https://whatportis.com/">SEE: port information</a>
 */
public class TcpPorts {
	/** Compressnet Management Utility */
	public static final int COMPRESSNET_MANAGEMENT_UTILITY = 2;

	/** Compressnet Compression Process */
	public static final int COMPRESSNET_COMPRESSION_PROCESS = 3;

	/** Remote Job Entry */
	public static final int RJE = 5;

	/** Echo */
	public static final int ECHO = 7;

	/** Discard */
	public static final int DISCARD = 9;

	/** SYSTAT */
	public static final int SYSTAT = 11;

	/** Daytime */
	public static final int DAYTIME = 13;

	/** Quote of the Day */
	public static final int QOTD = 17;

	/** Message Send Protocol */
	public static final int MSP = 18;

	/** Character Generator */
	public static final int CHARGEN = 19;

	/** File Transfer [Default Data] */
	public static final int FTP_DATA = 20;

	/** File Transfer [Control] */
	public static final int FTP = 21;

	/** The Secure Shell (SSH) */
	public static final int SSH = 22;

	/** Telnet */
	public static final int TELNET = 23;

	/** Simple Mail Transfer */
	public static final int SMTP = 25;

	/** NSW User System FE */
	public static final int NSW_FE = 27;

	/** MSG ICP */
	public static final int MSG_ICP = 29;

	/** MSG Authentication */
	public static final int MSG_AUTH = 31;

	/** Display Support Protocol */
	public static final int DSP = 33;

	/** Time */
	public static final int TIME = 37;

	/** Route Access Protocol */
	public static final int RAP = 38;

	/** Resource Location Protocol */
	public static final int RLP = 39;

	/** Graphics */
	public static final int GRAPHICS = 41;

	/** Host Name Server */
	public static final int NAMESERVER = 42;

	/** Who Is */
	public static final int WHOIS = 43;

	/** MPM FLAGS */
	public static final int MPM_FLAGS = 44;

	/** Message Processing Module [recv] */
	public static final int MPM = 45;

	/** Message Processing Module [default send] */
	public static final int MPM_SND = 46;

	/** NI FTP */
	public static final int NI_FTP = 47;

	/** Digital Audit Daemon */
	public static final int AUDITD = 48;

	/** Login Host Protocol (TACACS) */
	public static final int TACACS = 49;

	/** Remote Mail Checking Protocol */
	public static final int RE_MAIL_CK = 50;

	/** XNS Time Protocol */
	public static final int XNS_TIME = 52;

	/** Domain Name Server */
	public static final int DNS = 53;

	/** XNS Clearing house */
	public static final int XNS_CH = 54;

	/** ISI Graphics Language */
	public static final int ISI_GL = 55;

	/** XNS Authentication */
	public static final int XNS_AUTH = 56;

	/** XNS Mail */
	public static final int XNS_MAIL = 58;

	/** NI MAIL */
	public static final int NI_MAIL = 61;

	/** ACA Services */
	public static final int ACAS = 62;

	/** whois++ */
	public static final int WHOIS_PP = 63;

	/** Communications Integrator (CI) */
	public static final int COVIA = 64;

	/** TACACS-Database Service */
	public static final int TACACS_DS = 65;

	/** Oracle SQL*NET */
	public static final int ORACLE_SQL_NET = 66;

	/** Bootstrap Protocol Server */
	public static final int BOOTPS = 67;

	/** Bootstrap Protocol Client */
	public static final int BOOTPC = 68;

	/** Trivial File Transfer */
	public static final int TFTP = 69;

	/** GOPHER */
	public static final int GOPHER = 70;

	/** Remote Job Service 1 */
	public static final int NETRJS_1 = 71;

	/** Remote Job Service 2 */
	public static final int NETRJS_2 = 72;

	/** Remote Job Service 3 */
	public static final int NETRJS_3 = 73;

	/** Remote Job Service 4 */
	public static final int NETRJS_4 = 74;

	/** Distributed External Object Store */
	public static final int DEOS = 76;

	/** VETTCP */
	public static final int VETTCP = 78;

	/** Finger */
	public static final int FINGER = 79;

	/** HTTP */
	public static final int HTTP = 80;

	/** XFER Utility */
	public static final int XFER = 82;

	/** MIT ML Device */
	public static final int MIT_ML_DEV_83 = 83;

	/** Common Trace Facility */
	public static final int CTF = 84;

	/** MIT ML Device */
	public static final int MIT_ML_DEV_85 = 85;

	/** Micro Focus Cobol */
	public static final int MFCOBOL = 86;

	/** Kerberos */
	public static final int KERBEROS = 88;

	/** SU/MIT Telnet Gateway */
	public static final int SU_MIT_TG = 89;

	/** DNSIX Securit Attribute Token Map */
	public static final int DNSIX = 90;

	/** MIT Dover Spooler */
	public static final int MIT_DOV = 91;

	/** Network Printing Protocol */
	public static final int NPP = 92;

	/** Device Control Protocol */
	public static final int DCP = 93;

	/** Tivoli Object Dispatcher */
	public static final int OBJCALL = 94;

	/** SUPDUP */
	public static final int SUPDUP = 95;

	/** DIXIE Protocol Specification */
	public static final int DIXIE = 96;

	/** Swift Remote Virtural File Protocol */
	public static final int SWIFT_RVF = 97;

	/** TAC News */
	public static final int TACNEWS = 98;

	/** Metagram Relay */
	public static final int METAGRAM = 99;

	/** NIC Host Name Server */
	public static final int HOSTNAME = 101;

	/** ISO-TSAP Class 0 */
	public static final int ISO_TSAP = 102;

	/** Genesis Point-to-Point Trans Net */
	public static final int GPPITNP = 103;

	/** ACR-NEMA DICOM 300 */
	public static final int ACR_NEMA = 104;

	/** CCSO Nameserver Protocol */
	public static final int CSO = 105;

	/** 3COM-TSMUX */
	public static final int TCP_3COM_TSMUX = 106;

	/** Remote Telnet Service */
	public static final int RTELNET = 107;

	/** SNA Gateway Access Server */
	public static final int SNAGAS = 108;

	/** Post Office Protocol - Version 2 */
	public static final int POP2 = 109;

	/** Post Office Protocol - Version 3 */
	public static final int POP3 = 110;

	/** SUN Remote Procedure Call */
	public static final int SUNRPC = 111;

	/** McIDAS Data Transmission Protocol */
	public static final int MCIDAS = 112;

	/** Authentication Service */
	public static final int AUTH = 113;

	/** Simple File Transfer Protocol */
	public static final int SFTP = 115;

	/** ANSA REX Notify */
	public static final int ANSANOTIFY = 116;

	/** UUCP Path Service */
	public static final int UUCP_PATH = 117;

	/** SQL Services */
	public static final int SQLSERV = 118;

	/** Network News Transfer Protocol */
	public static final int NNTP = 119;

	/** CFDPTKT */
	public static final int CFDPTKT = 120;

	/** Encore Expedited Remote Pro.Call */
	public static final int ERPC = 121;

	/** SMAKYNET */
	public static final int SMAKYNET = 122;

	/** Network Time Protocol */
	public static final int NTP = 123;

	/** ANSA REX Trader */
	public static final int ANSATRADER = 124;

	/** Locus PC-Interface Net Map Server */
	public static final int LOCUS_MAP = 125;

	/** NXEdit */
	public static final int NXEDIT = 126;

	/** Locus PC-Interface Conn Server */
	public static final int LOCUS_CON = 127;

	/** GSS X License Verification */
	public static final int GSS_XLICEN = 128;

	/** Password Generator Protocol */
	public static final int PWDGEN = 129;

	/** Cisco FNATIVE */
	public static final int CISCO_FNA = 130;

	/** Cisco TNATIVE */
	public static final int CISCO_TNA = 131;

	/** Cisco SYSMAINT */
	public static final int CISCO_SYS = 132;

	/** Statistics Service */
	public static final int STATSRV = 133;

	/** INGRES-NET Service */
	public static final int INGRES_NET = 134;

	/** DCE endpoint resolution */
	public static final int EPMAP = 135;

	/** PROFILE Naming System */
	public static final int PROFILE = 136;

	/** NETBIOS Name Service */
	public static final int NETBIOS_NS = 137;

	/** NETBIOS Datagram Service */
	public static final int NETBIOS_DATAGRAM = 138;

	/** NETBIOS Session Service */
	public static final int NETBIOS_SESSION = 139;

	/** EMFIS Data Service */
	public static final int EMFIS_DATA = 140;

	/** EMFIS Control Service */
	public static final int EMFIS_CNTL = 141;

	/** Britton-Lee IDM */
	public static final int BL_IDM = 142;

	/** Internet Message Access Protocol */
	public static final int IMAP = 143;

	/** Universal Management Architecture */
	public static final int UMA = 144;

	/** UAAC Protocol */
	public static final int UAAC = 145;

	/** ISO-IP0 */
	public static final int ISO_TP0 = 146;

	/** ISO-IP */
	public static final int ISO_IP = 147;

	/** Jargon */
	public static final int JARGON = 148;

	/** AED 512 Emulation Service */
	public static final int AED_512 = 149;

	/** SQL-NET */
	public static final int SQL_NET = 150;

	/** HEMS */
	public static final int HEMS = 151;

	/** Background File Transfer Program */
	public static final int BFTP = 152;

	/** SGMP */
	public static final int SGMP = 153;

	/** NETSC */
	public static final int NETSC_PROD = 154;

	/** NETSC */
	public static final int NETSC_DEV = 155;

	/** SQL Service */
	public static final int SQLSRV = 156;

	/** KNET/VM Command/Message Protocol */
	public static final int KNET_CMP = 157;

	/** PCMail Server */
	public static final int PCMAIL_SRV = 158;

	/** NSS-Routing */
	public static final int NSS_ROUTING = 159;

	/** SGMP-TRAPS */
	public static final int SGMP_TRAPS = 160;

	/** SNMP */
	public static final int SNMP = 161;

	/** SNMP Trap */
	public static final int SNMP_TRAP = 162;

	/** CMIP/TCP Manager */
	public static final int CMIP_MAN = 163;

	/** CMIP/TCP Agent */
	public static final int CMIP_AGENT = 164;

	/** XNS Courier */
	public static final int XNS_COURIER = 165;

	/** Sirius Systems */
	public static final int S_NET = 166;

	/** NAMP */
	public static final int NAMP = 167;

	/** RSVD */
	public static final int RSVD = 168;

	/** SEND */
	public static final int SEND = 169;

	/** Network PostScript */
	public static final int PRINT_SRV = 170;

	/** Network Innovations Multiplex */
	public static final int MULTIPLEX = 171;

	/** Network Innovations CL/1 */
	public static final int CL_1 = 172;

	/** Xyplex */
	public static final int XYPLEX_MUX = 173;

	/** MAILQ */
	public static final int MAILQ = 174;

	/** VMNET */
	public static final int VMNET = 175;

	/** GENRAD-MUX */
	public static final int GENRAD_MUX = 176;

	/** X Display Manager Control Protocol */
	public static final int XDMCP = 177;

	/** NextStep Window Server */
	public static final int NEXTSTEP = 178;

	/** Border Gateway Protocol */
	public static final int BGP = 179;

	/** Intergraph */
	public static final int RIS = 180;

	/** Unify */
	public static final int UNIFY = 181;

	/** Unisys Audit SITP */
	public static final int AUDIT = 182;

	/** OCBinder */
	public static final int OCBINDER = 183;

	/** OCServer */
	public static final int OCSERVER = 184;

	/** Remote-KIS */
	public static final int REMOTE_KIS = 185;

	/** KIS Protocol */
	public static final int KIS = 186;

	/** Application Communication Interface */
	public static final int ACI = 187;

	/** Plus Five's MUMPS */
	public static final int MUMPS = 188;

	/** Queued File Transport */
	public static final int QFT = 189;

	/** Gateway Access Control Protocol */
	public static final int GACP = 190;

	/** Prospero Directory Service */
	public static final int PROSPERO = 191;

	/** OSU Network Monitoring System */
	public static final int OSU_NMS = 192;

	/** Spider Remote Monitoring Protocol */
	public static final int SRMP = 193;

	/** Internet Relay Chat Protocol */
	public static final int IRC = 194;

	/** DNSIX Network Level Module Audit */
	public static final int DN6_NLM_AUD = 195;

	/** DNSIX Session Mgt Module Audit Redir */
	public static final int DN6_SMM_RED = 196;

	/** Directory Location Service */
	public static final int DLS = 197;

	/** Directory Location Service Monitor */
	public static final int DLS_MON = 198;

	/** SMUX */
	public static final int SMUX = 199;

	/** IBM System Resource Controller */
	public static final int SRC = 200;

	/** AppleTalk Routing Maintenance */
	public static final int AT_RTMP = 201;

	/** AppleTalk Name Binding */
	public static final int AT_NBP = 202;

	/** AppleTalk Unused */
	public static final int AT_3 = 203;

	/** AppleTalk Echo */
	public static final int AT_ECHO = 204;

	/** AppleTalk Unused */
	public static final int AT_5 = 205;

	/** AppleTalk Zone Information */
	public static final int AT_ZIS = 206;

	/** AppleTalk Unused */
	public static final int AT_7 = 207;

	/** AppleTalk Unused */
	public static final int AT_8 = 208;

	/** The Quick Mail Transfer Protocol */
	public static final int QMTP = 209;

	/** ANSI Z39.50 */
	public static final int Z39_50 = 210;

	/** Texas Instruments 914C/G Terminal */
	public static final int TEXAS_INSTRUMENTS_914C_G = 211;

	/** ATEXSSTR */
	public static final int ANET = 212;

	/** IPX */
	public static final int IPX = 213;

	/** VM PWSCS */
	public static final int VMPWSCS = 214;

	/** Insignia Solutions SoftPC */
	public static final int SOFTPC = 215;

	/** Computer Associates Int'l License Server */
	public static final int CAILIC = 216;

	/** dBASE Unix */
	public static final int DBASE = 217;

	/** Netix Message Posting Protocol */
	public static final int MPP = 218;

	/** Unisys ARPs */
	public static final int UARPS = 219;

	/** Interactive Mail Access Protocol v3 */
	public static final int IMAP3 = 220;

	/** Berkeley rlogind with SPX auth */
	public static final int FLN_SPX = 221;

	/** Berkeley rshd with SPX auth */
	public static final int RSH_SPX = 222;

	/** Certificate Distribution Center */
	public static final int CDC = 223;

	/** masqdialer */
	public static final int MASQDIALER = 224;

	/** Direct */
	public static final int DIRECT = 242;

	/** Survey Measurement */
	public static final int SUR_MEAS = 243;

	/** inbusiness */
	public static final int INBUSINESS = 244;

	/** LINK */
	public static final int LINK = 245;

	/** Display Systems Protocol */
	public static final int DSP3270 = 246;

	/** SUBNTBCST_TFTP */
	public static final int SUBNTBCST_TFTP = 247;

	/** bhfhs */
	public static final int BHFHS = 248;

	/** Secure Electronic Transaction */
	public static final int SET = 257;

	/** Efficient Short Remote Operations */
	public static final int ESRO_GEN = 259;

	/** Openport */
	public static final int OPENPORT = 260;

	/** IIOP Name Service over TLS/SSL */
	public static final int NSIIOPS = 261;

	/** Arcisdms */
	public static final int ARCISDMS = 262;

	/** HDAP */
	public static final int HDAP = 263;

	/** BGMP */
	public static final int BGMP = 264;

	/** X-Bone CTL */
	public static final int X_BONE_CTL = 265;

	/** SCSI on ST */
	public static final int SST = 266;

	/** Tobit David Service Layer */
	public static final int TD_SERVICE = 267;

	/** Tobit David Replica */
	public static final int TD_REPLICA = 268;

	/** MANET Protocols */
	public static final int MANET = 269;

	/**
	 * IETF Network Endpoint Assessment (NEA) Posture Transport Protocol over TLS
	 * (PT-TLS): 271
	 */
	public static final int PT_TLS = 271;

	/** HTTP-Mgmt */
	public static final int HTTP_MGMT = 280;

	/** Personal Link */
	public static final int PERSONAL_LINK = 281;

	/** Cable Port A/X */
	public static final int CABLEPORT_AX = 282;

	/** rescap */
	public static final int RESCAP = 283;

	/** corerjd */
	public static final int CORERJD = 284;

	/** FXP Communication */
	public static final int FXP = 286;

	/** K-BLOCK */
	public static final int K_BLOCK = 287;

	/** Novastor Backup */
	public static final int NOVASTORBAKCUP = 308;

	/** EntrustTime */
	public static final int ENTRUSTTIME = 309;

	/** bhmds */
	public static final int BHMDS = 310;

	/** AppleShare IP WebAdmin */
	public static final int ASIP_WEBADMIN = 311;

	/** VSLMP */
	public static final int VSLMP = 312;

	/** Magenta Logic */
	public static final int MAGENTA_LOGIC = 313;

	/** Opalis Robot */
	public static final int OPALIS_ROBOT = 314;

	/** DPSI */
	public static final int DPSI = 315;

	/** decAuth */
	public static final int DECAUTH = 316;

	/** Zannet */
	public static final int ZANNET = 317;

	/** PKIX TimeStamp */
	public static final int PKIX_TIMESTAMP = 318;

	/** PTP Event */
	public static final int PTP_EVENT = 319;

	/** PTP General */
	public static final int PTP_GENERAL = 320;

	/** PIP */
	public static final int PIP = 321;

	/** RTSPS */
	public static final int RTSPS = 322;

	/** Resource PKI to Router Protocol */
	public static final int RPKI_RTR = 323;

	/** Resource PKI to Router Protocol over TLS */
	public static final int RPKI_RTR_TLS = 324;

	/** Texar Security Port */
	public static final int TEXAR = 333;

	/** Prospero Data Access Protocol */
	public static final int PDAP = 344;

	/** Perf Analysis Workbench */
	public static final int PAWSERV = 345;

	/** Zebra server */
	public static final int ZSERV = 346;

	/** Fatmen Server */
	public static final int FATSERV = 347;

	/** Cabletron Management Protocol */
	public static final int CSI_SGWP = 348;

	/** MFTP */
	public static final int MFTP = 349;

	/** MATIP Type A */
	public static final int MATIP_TYPE_A = 350;

	/** MATIP Type B */
	public static final int MATIP_TYPE_B = 351;

	/** DTAG */
	public static final int DTAG_STE_SB = 352;

	/** NDSAUTH */
	public static final int NDSAUTH = 353;

	/** bh611 */
	public static final int BH611 = 354;

	/** DATEX-ASN */
	public static final int DATEX_ASN = 355;

	/** Cloanto Net 1 */
	public static final int CLOANTO_NET_1 = 356;

	/** bhevent */
	public static final int BHEVENT = 357;

	/** Shrinkwrap */
	public static final int SHRINKWRAP = 358;

	/** Network Security Risk Management Protocol */
	public static final int NSRMP = 359;

	/** scoi2odialog */
	public static final int SCOI2ODIALOG = 360;

	/** Semantix */
	public static final int SEMANTIX = 361;

	/** SRS Send */
	public static final int SRSSEND = 362;

	/** RSVP Tunnel */
	public static final int RSVP_TUNNEL = 363;

	/** Aurora CMGR */
	public static final int AURORA_CMGR = 364;

	/** DTK */
	public static final int DTK = 365;

	/** ODMR */
	public static final int ODMR = 366;

	/** MortgageWare */
	public static final int MORTGAGEWARE = 367;

	/** QbikGDP */
	public static final int QBIKGDP = 368;

	/** rpc2portmap */
	public static final int RPC2PORTMAP = 369;

	/** codaauth2 */
	public static final int CODAAUTH2 = 370;

	/** Clearcase */
	public static final int CLEARCASE = 371;

	/** ListProcessor */
	public static final int ULISTPROC = 372;

	/** Legent Corporation */
	public static final int LEGENT_1 = 373;

	/** Legent Corporation */
	public static final int LEGENT_2 = 374;

	/** Hassle */
	public static final int HASSLE = 375;

	/** Amiga Envoy Network Inquiry Proto */
	public static final int NIP = 376;

	/** NEC Corporation tnETOS */
	public static final int TNETOS = 377;

	/** NEC Corporation dsETOS */
	public static final int DSETOS = 378;

	/** TIA/EIA/IS-99 modem client */
	public static final int IS99C = 379;

	/** TIA/EIA/IS-99 modem server */
	public static final int IS99S = 380;

	/** HP performance data collector */
	public static final int HP_COLLECTOR = 381;

	/** HP performance data managed node */
	public static final int HP_MANAGED_NODE = 382;

	/** HP performance data alarm manager */
	public static final int HP_ALARM_MGR = 383;

	/** A Remote Network Server System */
	public static final int ARNS = 384;

	/** IBM Application */
	public static final int IBM_APP = 385;

	/** ASA Message Router Object Def. */
	public static final int ASA = 386;

	/** Appletalk Update-Based Routing Pro. */
	public static final int AURP = 387;

	/** Unidata LDM */
	public static final int UNIDATA_LDM = 388;

	/** Lightweight Directory Access Protocol */
	public static final int LDAP = 389;

	/** UIS */
	public static final int UIS = 390;

	/** SynOptics SNMP Relay Port */
	public static final int SYNOTICS_RELAY = 391;

	/** SynOptics Port Broker Port */
	public static final int SYNOTICS_BROKER = 392;

	/** Meta5 */
	public static final int META5 = 393;

	/** EMBL Nucleic Data Transfer */
	public static final int EMBL_NDT = 394;

	/** NetScout Control Protocol */
	public static final int NETCP = 395;

	/** Novell Netware over IP */
	public static final int NETWARE_IP = 396;

	/** Multi Protocol Trans. Net. */
	public static final int MPTN = 397;

	/** Kryptolan */
	public static final int KRYPTOLAN = 398;

	/** ISO Transport Class 2 Non-Control over TCP */
	public static final int ISO_TSAP_C2 = 399;

	/** Oracle Secure Backup */
	public static final int OSB_SD = 400;

	/** Uninterruptible Power Supply */
	public static final int UPS = 401;

	/** Genie Protocol */
	public static final int GENIE = 402;

	/** decap */
	public static final int DECAP = 403;

	/** nced */
	public static final int NCED = 404;

	/** ncld */
	public static final int NCLD = 405;

	/** Interactive Mail Support Protocol */
	public static final int IMSP = 406;

	/** Timbuktu */
	public static final int TIMBUKTU = 407;

	/** Prospero Resource Manager Sys. Man. */
	public static final int PRM_SM = 408;

	/** Prospero Resource Manager Node Man. */
	public static final int PRM_NM = 409;

	/** DECLadebug Remote Debug Protocol */
	public static final int DECLADEBUG = 410;

	/** Remote MT Protocol */
	public static final int RMT = 411;

	/** Trap Convention Port */
	public static final int SYNOPTICS_TRAP = 412;

	/** Storage Management Services Protocol */
	public static final int SMSP = 413;

	/** InfoSeek */
	public static final int INFOSEEK = 414;

	/** BNet */
	public static final int BNET = 415;

	/** Silverplatter */
	public static final int SILVERPLATTER = 416;

	/** Onmux */
	public static final int ONMUX = 417;

	/** Hyper-G */
	public static final int HYPER_G = 418;

	/** Ariel 1 */
	public static final int ARIEL1 = 419;

	/** SMPTE */
	public static final int SMPTE = 420;

	/** Ariel 2 */
	public static final int ARIEL2 = 421;

	/** Ariel 3 */
	public static final int ARIEL3 = 422;

	/** IBM Operations Planning and Control Start */
	public static final int OPC_JOB_START = 423;

	/** IBM Operations Planning and Control Track */
	public static final int OPC_JOB_TRACK = 424;

	/** ICAD */
	public static final int ICAD_EL = 425;

	/** smartsdp */
	public static final int SMARTSDP = 426;

	/** Server Location */
	public static final int SVRLOC = 427;

	/** OCS_CMU */
	public static final int OCS_CMU = 428;

	/** OCS_AMU */
	public static final int OCS_AMU = 429;

	/** UTMPSD */
	public static final int UTMPSD = 430;

	/** UTMPCD */
	public static final int UTMPCD = 431;

	/** IASD */
	public static final int IASD = 432;

	/** NNSP */
	public static final int NNSP = 433;

	/** MobileIP-Agent */
	public static final int MOBILEIP_AGENT = 434;

	/** MobilIP-MN */
	public static final int MOBILIP_MN = 435;

	/** DNA-CML */
	public static final int DNA_CML = 436;

	/** comscm */
	public static final int COMSCM = 437;

	/** dsfgw */
	public static final int DSFGW = 438;

	/** dasp */
	public static final int DASP = 439;

	/** sgcp */
	public static final int SGCP = 440;

	/** decvms-sysmgt */
	public static final int DECVMS_SYSMGT = 441;

	/** cvc_hostd */
	public static final int CVC_HOSTD = 442;

	/** HTTPS */
	public static final int HTTPS = 443;

	/** Simple Network Paging Protocol */
	public static final int SNPP = 444;

	/** SMB: Microsoft-DS */
	public static final int SMB = 445;

	/** DDM-Remote Relational Database Access */
	public static final int DDM_RDB = 446;

	/** DDM-Distributed File Management */
	public static final int DDM_DFM = 447;

	/** DDM-Remote DB Access Using Secure Sockets */
	public static final int DDM_SSL = 448;

	/** AS Server Mapper */
	public static final int AS_SERVERMAP = 449;

	/** Computer Supported Telecomunication Applications */
	public static final int TSERVER = 450;

	/** Cray Network Semaphore server */
	public static final int SFS_SMP_NET = 451;

	/** Cray SFS config server */
	public static final int SFS_CONFIG = 452;

	/** CreativeServer */
	public static final int CREATIVESERVER = 453;

	/** ContentServer */
	public static final int CONTENTSERVER = 454;

	/** CreativePartnr */
	public static final int CREATIVEPARTNR = 455;

	/** macon-tcp */
	public static final int MACON_TCP = 456;

	/** scohelp */
	public static final int SCOHELP = 457;

	/** apple quick time */
	public static final int APPLEQTC = 458;

	/** ampr-rcmd */
	public static final int AMPR_RCMD = 459;

	/** skronk */
	public static final int SKRONK = 460;

	/** DataRampSrv */
	public static final int DATASURFSRV = 461;

	/** DataRampSrvSec */
	public static final int DATASURFSRVSEC = 462;

	/** alpes */
	public static final int ALPES = 463;

	/** kpasswd */
	public static final int KPASSWD = 464;

	/** URL Rendesvous Directory for SSM */
	public static final int URD = 465;

	/** digital-vrc */
	public static final int DIGITAL_VRC = 466;

	/** mylex-mapd */
	public static final int MYLEX_MAPD = 467;

	/** proturis */
	public static final int PHOTURIS = 468;

	/** Radio Control Protocol */
	public static final int RCP = 469;

	/** scx-proxy */
	public static final int SCX_PROXY = 470;

	/** Mondex */
	public static final int MONDEX = 471;

	/** ljk-login */
	public static final int LJK_LOGIN = 472;

	/** hybrid-pop */
	public static final int HYBRID_POP = 473;

	/** tn-tl-w1 */
	public static final int TN_TL_W1 = 474;

	/** tcpnethaspsrv */
	public static final int TCPNETHASPSRV = 475;

	/** tn-tl-fd1 */
	public static final int TN_TL_FD1 = 476;

	/** ss7ns */
	public static final int SS7NS = 477;

	/** spsc */
	public static final int SPSC = 478;

	/** iafserver */
	public static final int IAFSERVER = 479;

	/** iafdbase */
	public static final int IAFDBASE = 480;

	/** Ph service */
	public static final int PH = 481;

	/** bgs-nsi */
	public static final int BGS_NSI = 482;

	/** ulpnet */
	public static final int ULPNET = 483;

	/** Integra Software Management Environment */
	public static final int INTEGRA_SME = 484;

	/** Air Soft Power Burst */
	public static final int POWERBURST = 485;

	/** avian */
	public static final int AVIAN = 486;

	/** Simple Asynchronous File Transfer */
	public static final int SAFT = 487;

	/** GSS-HTTP */
	public static final int GSS_HTTP = 488;

	/** nest-protocol */
	public static final int NEST_PROTOCOL = 489;

	/** micom-pfs */
	public static final int MICOM_PFS = 490;

	/** go-login */
	public static final int GO_LOGIN = 491;

	/** Transport Independent Convergence for FNA */
	public static final int TICF_1 = 492;

	/** Transport Independent Convergence for FNA */
	public static final int TICF_2 = 493;

	/** POV-Ray */
	public static final int POV_RAY = 494;

	/** intecourier */
	public static final int INTECOURIER = 495;

	/** PIM-RP-DISC */
	public static final int PIM_RP_DISC = 496;

	/** Retrospect backup and restore service */
	public static final int RETROSPECT = 497;

	/** siam */
	public static final int SIAM = 498;

	/** ISO ILL Protocol */
	public static final int ISO_ILL = 499;

	/** isakmp */
	public static final int ISAKMP = 500;

	/** STMF */
	public static final int STMF = 501;

	/** Modbus Application Protocol */
	public static final int MBAP = 502;

	/** Intrinsa */
	public static final int INTRINSA = 503;

	/** citadel */
	public static final int CITADEL = 504;

	/** mailbox-lm */
	public static final int MAILBOX_LM = 505;

	/** ohimsrv */
	public static final int OHIMSRV = 506;

	/** crs */
	public static final int CRS = 507;

	/** xvttp */
	public static final int XVTTP = 508;

	/** snare */
	public static final int SNARE = 509;

	/** FirstClass Protocol */
	public static final int FCP = 510;

	/** PassGo */
	public static final int PASSGO = 511;

	/** exec */
	public static final int EXEC = 512;

	/** login */
	public static final int LOGIN = 513;

	/** shell */
	public static final int SHELL = 514;

	/** spooler */
	public static final int PRINTER = 515;

	/** videotex */
	public static final int VIDEOTEX = 516;

	/** TALK */
	public static final int TALK = 517;

	/** ntalk */
	public static final int NTALK = 518;

	/** unixtime */
	public static final int UTIME = 519;

	/** extended file name server */
	public static final int EFS = 520;

	/** ripng */
	public static final int RIPNG = 521;

	/** ULP */
	public static final int ULP = 522;

	/** IBM-DB2 */
	public static final int IBM_DB2 = 523;

	/** NCP */
	public static final int NCP = 524;

	/** timeserver */
	public static final int TIMED = 525;

	/** newdate */
	public static final int TEMPO = 526;

	/** Stock IXChange */
	public static final int STX = 527;

	/** Customer IXChange */
	public static final int CUSTIX = 528;

	/** IRC-SERV */
	public static final int IRC_SERV = 529;

	/** courier */
	public static final int COURIER = 530;

	/** conference */
	public static final int CONFERENCE = 531;

	/** readnews */
	public static final int NETNEWS = 532;

	/** netwall */
	public static final int NETWALL = 533;

	/** windream Admin */
	public static final int WINDREAM = 534;

	/** iiop */
	public static final int IIOP = 535;

	/** opalis-rdv */
	public static final int OPALIS_RDV = 536;

	/** Networked Media Streaming Protocol */
	public static final int NMSP = 537;

	/** gdomap */
	public static final int GDOMAP = 538;

	/** Apertus Technologies Load Determination */
	public static final int APERTUS_LDP = 539;

	/** uucpd */
	public static final int UUCP = 540;

	/** uucp-rlogin */
	public static final int UUCP_RLOGIN = 541;

	/** commerce */
	public static final int COMMERCE = 542;

	/** klogin */
	public static final int KLOGIN = 543;

	/** krcmd */
	public static final int KSHELL = 544;

	/** appleqtcsrvr */
	public static final int APPLEQTCSRVR = 545;

	/** DHCPv6 Client */
	public static final int DHCPV6_CLIENT = 546;

	/** DHCPv6 Server */
	public static final int DHCPV6_SERVER = 547;

	/** AFP over TCP */
	public static final int AFPOVERTCP = 548;

	/** IDFP */
	public static final int IDFP = 549;

	/** new-who */
	public static final int NEW_RWHO = 550;

	/** cybercash */
	public static final int CYBERCASH = 551;

	/** DeviceShare */
	public static final int DEVSHR_NTS = 552;

	/** pirp */
	public static final int PIRP = 553;

	/** Real Time Streaming Protocol (RTSP) */
	public static final int RTSP = 554;

	/** dsf */
	public static final int DSF = 555;

	/** rfs server */
	public static final int REMOTEFS = 556;

	/** openvms-sysipc */
	public static final int OPENVMS_SYSIPC = 557;

	/** SDNSKMP */
	public static final int SDNSKMP = 558;

	/** TEEDTAP */
	public static final int TEEDTAP = 559;

	/** rmonitord */
	public static final int RMONITOR = 560;

	/** monitor */
	public static final int MONITOR = 561;

	/** chcmd */
	public static final int CHSHELL = 562;

	/** nntp protocol over TLS/SSL (was snntp) */
	public static final int NNTPS = 563;

	/** plan 9 file service */
	public static final int TCP_9PFS = 564;

	/** whoami */
	public static final int WHOAMI = 565;

	/** streettalk */
	public static final int STREETTALK = 566;

	/** banyan-rpc */
	public static final int BANYAN_RPC = 567;

	/** microsoft shuttle */
	public static final int MS_SHUTTLE = 568;

	/** microsoft rome */
	public static final int MS_ROME = 569;

	/** meter demon */
	public static final int METER_DEMON = 570;

	/** meter udemon */
	public static final int METER_UDEMON = 571;

	/** sonar */
	public static final int SONAR = 572;

	/** banyan-vip */
	public static final int BANYAN_VIP = 573;

	/** FTP Software Agent System */
	public static final int FTP_AGENT = 574;

	/** VEMMI */
	public static final int VEMMI = 575;

	/** ipcd */
	public static final int IPCD = 576;

	/** vnas */
	public static final int VNAS = 577;

	/** ipdd */
	public static final int IPDD = 578;

	/** decbsrv */
	public static final int DECBSRV = 579;

	/** SNTP HEARTBEAT */
	public static final int SNTP_HEARTBEAT = 580;

	/** Bundle Discovery Protocol */
	public static final int BDP = 581;

	/** SCC Security */
	public static final int SCC_SECURITY = 582;

	/** Philips Video-Conferencing */
	public static final int PHILIPS_VC = 583;

	/** Key Server */
	public static final int KEYSERVER = 584;

	/** Password Change */
	public static final int PASSWORD_CHG = 586;

	/** Message Submission */
	public static final int SUBMISSION = 587;

	/** CAL */
	public static final int CAL = 588;

	/** EyeLink */
	public static final int EYELINK = 589;

	/** TNS CML */
	public static final int TNS_CML = 590;

	/** FileMaker HTTP Alternate */
	public static final int HTTP_ALT = 591;

	/** Eudora Set */
	public static final int EUDORA_SET = 592;

	/** HTTP RPC Ep Map */
	public static final int HTTP_RPC_EPMAP = 593;

	/** TPIP */
	public static final int TPIP = 594;

	/** CAB Protocol */
	public static final int CAB_PROTOCOL = 595;

	/** SMSD */
	public static final int SMSD = 596;

	/** PTC Name Service */
	public static final int PTCNAMESERVICE = 597;

	/** SCO Web Server Manager 3 */
	public static final int SCO_WEBSRVRMG3 = 598;

	/** Aeolon Core Protocol */
	public static final int ACP = 599;

	/** Sun IPC server */
	public static final int IPCSERVER = 600;

	/** Reliable Syslog Service */
	public static final int SYSLOG_CONN = 601;

	/** XML-RPC over BEEP */
	public static final int XMLRPC_BEEP = 602;

	/** IDXP */
	public static final int IDXP = 603;

	/** TUNNEL */
	public static final int TUNNEL = 604;

	/** SOAP over BEEP */
	public static final int SOAP_BEEP = 605;

	/** Cray Unified Resource Manager */
	public static final int URM = 606;

	/** nqs */
	public static final int NQS = 607;

	/** Sender-Initiated/Unsolicited File Transfer */
	public static final int SIFT_UFT = 608;

	/** npmp-trap */
	public static final int NPMP_TRAP = 609;

	/** npmp-local */
	public static final int NPMP_LOCAL = 610;

	/** npmp-gui */
	public static final int NPMP_GUI = 611;

	/** HMMP Indication */
	public static final int HMMP_IND = 612;

	/** HMMP Operation */
	public static final int HMMP_OP = 613;

	/** SSLshell */
	public static final int SSHELL = 614;

	/** SCO Internet Configuration Manager */
	public static final int SCO_INETMGR = 615;

	/** SCO System Administration Server */
	public static final int SCO_SYSMGR = 616;

	/** SCO Desktop Administration Server */
	public static final int SCO_DTMGR = 617;

	/** DEI-ICDA */
	public static final int DEI_ICDA = 618;

	/** Compaq EVM */
	public static final int COMPAQ_EVM = 619;

	/** SCO WebServer Manager */
	public static final int SCO_WEBSRVRMGR = 620;

	/** ESCP */
	public static final int ESCP_IP = 621;

	/** Collaborator */
	public static final int COLLABORATOR = 622;

	/** DMTF out-of-band web services management protocol */
	public static final int OOB_WS_HTTP = 623;

	/** Crypto Admin */
	public static final int CRYPTOADMIN = 624;

	/** DEC DLM */
	public static final int DEC_DLM = 625;

	/** ASIA */
	public static final int ASIA = 626;

	/** PassGo Tivoli */
	public static final int PASSGO_TIVOLI = 627;

	/** QMQP */
	public static final int QMQP = 628;

	/** 3Com AMP3 */
	public static final int TCP_3COM_AMP3 = 629;

	/** RDA */
	public static final int RDA = 630;

	/** IPP (Internet Printing Protocol) */
	public static final int IPP = 631;

	/** bmpp */
	public static final int BMPP = 632;

	/** Service Status update (Sterling Software) */
	public static final int SERVSTAT = 633;

	/** ginad */
	public static final int GINAD = 634;

	/** RLZ DBase */
	public static final int RLZDBASE = 635;

	/** ldap protocol over TLS/SSL (was sldap) */
	public static final int LDAPS = 636;

	/** lanserver */
	public static final int LANSERVER = 637;

	/** mcns-sec */
	public static final int MCNS_SEC = 638;

	/** MSDP */
	public static final int MSDP = 639;

	/** entrust-sps */
	public static final int ENTRUST_SPS = 640;

	/** repcmd */
	public static final int REPCMD = 641;

	/** ESRO-EMSDP V1.3 */
	public static final int ESRO_EMSDP = 642;

	/** SANity */
	public static final int SANITY = 643;

	/** dwr */
	public static final int DWR = 644;

	/** PSSC */
	public static final int PSSC = 645;

	/** LDP */
	public static final int LDP = 646;

	/** DHCP Fail over */
	public static final int DHCP_FAILOVER = 647;

	/** Registry Registrar Protocol (RRP) */
	public static final int RRP = 648;

	/** Cadview-3d */
	public static final int CADVIEW_3D = 649;

	/** OBEX */
	public static final int OBEX = 650;

	/** IEEE MMS */
	public static final int IEEE_MMS = 651;

	/** HELLO_PORT */
	public static final int HELLO_PORT = 652;

	/** RepCmd */
	public static final int REPSCMD = 653;

	/** AODV */
	public static final int AODV = 654;

	/** TINC */
	public static final int TINC = 655;

	/** SPMP */
	public static final int SPMP = 656;

	/** RMC */
	public static final int RMC = 657;

	/** TenFold */
	public static final int TENFOLD = 658;

	/** MacOS Server Administration */
	public static final int MAC_SRVR_ADMIN = 660;

	/** HAP */
	public static final int HAP = 661;

	/** PFTP */
	public static final int PFTP = 662;

	/** PureNoise */
	public static final int PURENOISE = 663;

	/** DMTF out-of-band secure web services management protocol */
	public static final int OOB_WS_HTTPS = 664;

	/** Sun DR */
	public static final int SUN_DR = 665;

	/** doom Id Software */
	public static final int DOOM = 666;

	/** campaign contribution disclosures */
	public static final int DISCLOSE = 667;

	/** MeComm */
	public static final int MECOMM = 668;

	/** MeRegister */
	public static final int MEREGISTER = 669;

	/** VACDSM-SWS */
	public static final int VACDSM_SWS = 670;

	/** VACDSM-APP */
	public static final int VACDSM_APP = 671;

	/** VPPS-QUA */
	public static final int VPPS_QUA = 672;

	/** CIMPLEX */
	public static final int CIMPLEX = 673;

	/** ACAP */
	public static final int ACAP = 674;

	/** DCTP */
	public static final int DCTP = 675;

	/** VPPS Via */
	public static final int VPPS_VIA = 676;

	/** Virtual Presence Protocol */
	public static final int VPP = 677;

	/** GNU Generation Foundation NCP */
	public static final int GGF_NCP = 678;

	/** MRM */
	public static final int MRM = 679;

	/** entrust-aaas */
	public static final int ENTRUST_AAAS = 680;

	/** entrust-aams */
	public static final int ENTRUST_AAMS = 681;

	/** XFR */
	public static final int XFR = 682;

	/** CORBA IIOP */
	public static final int CORBA_IIOP = 683;

	/** CORBA IIOP SSL */
	public static final int CORBA_IIOP_SSL = 684;

	/** MDC Port Mapper */
	public static final int MDC_PORTMAPPER = 685;

	/** Hardware Control Protocol Wismar */
	public static final int HCP_WISMAR = 686;

	/** asipregistry */
	public static final int ASIPREGISTRY = 687;

	/** ApplianceWare managment protocol */
	public static final int REALM_RUSD = 688;

	/** NMAP */
	public static final int NMAP = 689;

	/** Velneo Application Transfer Protocol */
	public static final int VATP = 690;

	/** MS Exchange Routing */
	public static final int MSEXCH_ROUTING = 691;

	/** Hyperwave-ISP */
	public static final int HYPERWAVE_ISP = 692;

	/** almanid Connection Endpoint */
	public static final int CONNENDP = 693;

	/** ha-cluster */
	public static final int HA_CLUSTER = 694;

	/** IEEE-MMS-SSL */
	public static final int IEEE_MMS_SSL = 695;

	/** RUSHD */
	public static final int RUSHD = 696;

	/** UUIDGEN */
	public static final int UUIDGEN = 697;

	/** OLSR */
	public static final int OLSR = 698;

	/** Access Network */
	public static final int ACCESSNETWORK = 699;

	/** Extensible Provisioning Protocol */
	public static final int EPP = 700;

	/** Link Management Protocol (LMP) */
	public static final int LMP = 701;

	/** IRIS over BEEP */
	public static final int IRIS_BEEP = 702;

	/** errlog copy/server daemon */
	public static final int ELCSD = 704;

	/** AgentX */
	public static final int AGENTX = 705;

	/** SILC */
	public static final int SILC = 706;

	/** Borland DSJ */
	public static final int BORLAND_DSJ = 707;

	/** Entrust Key Management Service Handler */
	public static final int ENTRUST_KMSH = 709;

	/** Entrust Administration Service Handler */
	public static final int ENTRUST_ASH = 710;

	/** Cisco TDP */
	public static final int CISCO_TDP = 711;

	/** TBRPF */
	public static final int TBRPF = 712;

	/** IRIS over XPC */
	public static final int IRIS_XPC = 713;

	/** IRIS over XPCS */
	public static final int IRIS_XPCS = 714;

	/** IRIS-LWZ */
	public static final int IRIS_LWZ = 715;

	/** IBM NetView DM/6000 Server/Client */
	public static final int NETVIEWDM1 = 729;

	/** IBM NetView DM/6000 send/tcp */
	public static final int NETVIEWDM2 = 730;

	/** IBM NetView DM/6000 receive/tcp */
	public static final int NETVIEWDM3 = 731;

	/** netGW */
	public static final int NETGW = 741;

	/** Network based Rev. Cont. Sys. */
	public static final int NETRCS = 742;

	/** Flexible License Manager */
	public static final int FLEXLM = 744;

	/** Fujitsu Device Control */
	public static final int FUJITSU_DEV = 747;

	/** Russell Info Sci Calendar Manager */
	public static final int RIS_CM = 748;

	/** kerberos administration */
	public static final int KERBEROS_ADM = 749;

	/** rfile */
	public static final int RFILE = 750;

	/** pump */
	public static final int PUMP = 751;

	/** qrh */
	public static final int QRH = 752;

	/** rrh */
	public static final int RRH = 753;

	/** send */
	public static final int TELL = 754;

	/** nlogin */
	public static final int NLOGIN = 758;

	/** con */
	public static final int CON = 759;

	/** ns */
	public static final int NS = 760;

	/** RXE */
	public static final int RXE = 761;

	/** QUOTAD */
	public static final int QUOTAD = 762;

	/** cycleserv */
	public static final int CYCLESERV = 763;

	/** omserv */
	public static final int OMSERV = 764;

	/** webster */
	public static final int WEBSTER = 765;

	/** phone */
	public static final int PHONEBOOK = 767;

	/** VID */
	public static final int VID = 769;

	/** cadlock */
	public static final int CADLOCK = 770;

	/** rtip */
	public static final int RTIP = 771;

	/** cycleserv2 */
	public static final int CYCLESERV2 = 772;

	/** submit */
	public static final int SUBMIT = 773;

	/** RPASSWD */
	public static final int RPASSWD = 774;

	/** ENTOMB */
	public static final int ENTOMB = 775;

	/** WPAGES */
	public static final int WPAGES = 776;

	/** Multiling HTTP */
	public static final int MULTILING_HTTP = 777;

	/** wpgs */
	public static final int WPGS = 780;

	/** mdbs-daemon */
	public static final int MDBS_DAEMON = 800;

	/** device */
	public static final int DEVICE = 801;

	/** Modbus Application Protocol Secure */
	public static final int MBAP_S = 802;

	/** FCP */
	public static final int FCP_UDP = 810;

	/** itm-mcell-s */
	public static final int ITM_MCELL_S = 828;

	/** PKIX-3 CA/RA */
	public static final int PKIX_3_CA_RA = 829;

	/** NETCONF over SSH */
	public static final int NETCONF_SSH = 830;

	/** NETCONF over BEEP */
	public static final int NETCONF_BEEP = 831;

	/** NETCONF for SOAP over HTTPS */
	public static final int NETCONFSOAPHTTP = 832;

	/** NETCONF for SOAP over BEEP */
	public static final int NETCONFSOAPBEEP = 833;

	/** dhcp-failover 2 */
	public static final int DHCP_FAILOVER2 = 847;

	/** GDOI */
	public static final int GDOI = 848;

	/** iSCSI */
	public static final int ISCSI = 860;

	/** OWAMP-Control */
	public static final int OWAMP_CONTROL = 861;

	/** Two-way Active Measurement Protocol (TWAMP) Control */
	public static final int TWAMP_CONTROL = 862;

	/** rsync */
	public static final int RSYNC = 873;

	/** ICL coNETion locate server */
	public static final int ICLCNET_LOCATE = 886;

	/** ICL coNETion server info */
	public static final int ICLCNET_SVINFO = 887;

	/** AccessBuilder */
	public static final int ACCESSBUILDER = 888;

	/** OMG Initial Refs */
	public static final int OMGINITIALREFS = 900;

	/** SMPNAMERES */
	public static final int SMPNAMERES = 901;

	/** self documenting Telnet Door */
	public static final int IDEAFARM_DOOR = 902;

	/** self documenting Telnet Panic Door */
	public static final int IDEAFARM_PANIC = 903;

	/** Kerberized Internet Negotiation of Keys (KINK) */
	public static final int KINK = 910;

	/** xact-backup */
	public static final int XACT_BACKUP = 911;

	/** APEX relay-relay service */
	public static final int APEX_MESH = 912;

	/** APEX endpoint-relay service */
	public static final int APEX_EDGE = 913;

	/** ftp protocol, data, over TLS/SSL */
	public static final int FTPS_DATA = 989;

	/** ftp protocol, control, over TLS/SSL */
	public static final int FTPS = 990;

	/** Netnews Administration System */
	public static final int NAS = 991;

	/** telnet protocol over TLS/SSL */
	public static final int TELNETS = 992;

	/** imap4 protocol over TLS/SSL */
	public static final int IMAPS = 993;

	/** pop3 protocol over TLS/SSL (was spop3) */
	public static final int POP3S = 995;

	/** VSINET */
	public static final int VSINET = 996;

	/** MAITRD */
	public static final int MAITRD = 997;

	/** busboy */
	public static final int BUSBOY = 998;

	/** pup router */
	public static final int PUPROUTER = 999;

	/** cadlock2 */
	public static final int CADLOCK2 = 1000;
	
	/** ORACLE */
	public static final int ORACLE = 1521;
	
	/** MYSQL */
	public static final int MYSQL = 3306;
	
	/** RDP */
	public static final int RDP = 3389;
	
	/** PostgreSQL */
	public static final int PostgreSQL = 5432;
	
	/** REDIS */
	public static final int REDIS = 6379;
	
	/** Kubernetes */
	public static final int KubernetesAPI = 6443;
	
	/** Hadoop ‌NameNode */
	public static final int Hadoop‌NameNode = 50070;
	
	public static final int Synology = 5000;
	
	public static final int HTTP_8080 = 8080;
	
	public static final int HTTP_8089 = 8089;
	
	public static final int HTTP_3000 = 3000;

	public static Map<String, Integer> name2Value = new HashMap<String, Integer>();

	public static Map<Integer, String> value2Name = new HashMap<Integer, String>();

	/**
	 * Lists the static fields of the specified class whose names match the given
	 * prefix, and stores the name–value pairs in the provided Maps
	 * 
	 * @param clazz         The class that has static fields.
	 * @param name2ValueMap The map that stores name-value pairs
	 * @param value2NameMap The map that stores value-name pairs map
	 */
	private static void listFields(Class<?> clazz, String prefix, Map<String, Integer> name2ValueMap,
			Map<Integer, String> value2NameMap) {

		Field[] fields = clazz.getDeclaredFields();

		// iterate each field
		for (Field field : fields) {
			int modifiers = field.getModifiers();
			if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {
				String fieldName = field.getName();
				// if prefix is specified
				if (prefix != null && prefix.length() > 0) {
					if (!fieldName.startsWith(prefix))
						continue;
				}
				// if field data type match
				Class<?> fieldType = field.getType();
				if (fieldType.equals(int.class)) {
					try {
						int value = field.getInt(clazz);
						if (name2ValueMap != null)
							name2ValueMap.put(fieldName, value);
						if (value2NameMap != null)
							value2NameMap.put(value, fieldName);
					} catch (Exception e) {
					}
				}
			}
		}
	}

	static {
		listFields(TcpPorts.class, null, name2Value, value2Name);
	}

	/** return protocol name by protocol int */
	public static String getName(int value) {
		return value2Name.getOrDefault(value, "");
	}

	/** return protocol int by protocol name */
	public static int getValue(String name) {
		return name2Value.getOrDefault(name.toUpperCase(), 0);
	}

	
	public static int[] MOST_COMMON = new int[] {
			HTTP, HTTPS, FTP, SSH, TELNET, SMTP, POP3, IMAP, SNMP,
			SMB, NETBIOS_NS, NETBIOS_SESSION, Synology, 
			HTTP_8080, HTTP_8089, HTTP_3000, 
			REDIS, ORACLE, MYSQL
		};
}
