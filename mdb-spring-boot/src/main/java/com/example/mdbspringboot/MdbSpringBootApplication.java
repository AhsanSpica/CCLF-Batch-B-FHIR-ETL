package com.example.mdbspringboot;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;
import org.hl7.fhir.r4.model.*;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import org.hl7.fhir.dstu3.model.EligibilityResponse;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ExplanationOfBenefit.*;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Location.LocationStatus;
import org.hl7.fhir.r4.model.Money;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Timing;
import org.hl7.fhir.r4.model.Type;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Claim;
import org.hl7.fhir.r4.model.Claim.ClaimStatus;
import org.hl7.fhir.r4.model.ExplanationOfBenefit;
import org.hl7.fhir.r4.model.ExplanationOfBenefit.ExplanationOfBenefitStatus;
import org.json.JSONException;
import org.json.JSONObject;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Practitioner;
import com.example.mdbspringboot.model.Encounter;
import org.hl7.fhir.r5.model.*;

//import com.example.mdbspringboot.model.*;

import org.hl7.fhir.r4.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.hl7.fhir.r5.model.ContactPoint.ContactPointSystem.PHONE;
import static org.hl7.fhir.r5.model.ContactPoint.ContactPointUse.HOME;
//import org.springframework.cloud.client.ServiceInstance;
//import org.springframework.cloud.client.discovery.DiscoveryClient;

@SpringBootApplication
@EnableMongoRepositories
//@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
//public class MdbSpringBootApplication{
public class MdbSpringBootApplication implements CommandLineRunner{
	private Map<String, Encounter> patientMap = new HashMap<String, Encounter>();

	@Autowired
	PatFHIRRepo patientRepository;
	//	@Autowired
//	PractitionerRepo practitionerRepository;
	@Autowired
	EncounterRepo encounterRepository;

	//@Autowired
//	ItemRepository groceryItemRepo;

//	@Autowired
//	CustomItemRepository customRepo;
//	List<GroceryItem> itemList = new ArrayList<GroceryItem>();


	public static void main(String[] args) {
		SpringApplication.run(MdbSpringBootApplication.class, args);
	}
	//	@Bean
//	public ServletRegistrationBean ServletRegistrationBean(){
//		ServletRegistrationBean registration=new ServletRegistrationBean(new SimpleRestfulServer(),"/*");
//		registration.setName("FhirServlet");
//		return registration;
//	}
	public void run (String... args)
	{
		FhirContext ctx = FhirContext.forR4();
		IParser jsonparser = ctx.newJsonParser();
		Patient patient = new Patient();
		String orgname="";
		String orgid="";
		int rowCount=0;
		String serOrg  ="";
		int cliniccount = 0;
		int praccount = 0;
		int patcount = 0;
		int claimcount = 0;
		int eobcount = 0;
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		org.hl7.fhir.r4.model.Meta clmeta = new org.hl7.fhir.r4.model.Meta() ;
		clmeta.setId("Batch: "+now);
		try{}
		catch(Exception ex){}
		try
		{
			Class.forName("org.postgresql.Driver");
			Connection conn = DriverManager.getConnection(
					"jdbc:postgresql://44.203.188.74:5432/dataqhealth",
				//	"jdbc:postgresql://3.236.102.222:5432/cclfdb",
					"postgres", "Dataqhealth1");
			Statement stmt = conn.createStatement();
			//fetch list of prac
			List practlist = new LinkedList();
			List patlist = new LinkedList();
			List acolist = new LinkedList();
			List clinlist = new LinkedList();
			//fetching lists
			//fetching lists of clinic

			String clinsqlstr=" select distinct npi as location_identifier_value FROM public.npidata ";
			clinsqlstr +=" where npidata.entity_type_code ='2' and npi in (select DISTINCT fac_prvdr_npi_num from c_c_l_f1_s )";
			clinsqlstr +=" order by npi ";
		//	Statement stmta = conn.createStatement();
			//ResultSet clinlistRS = stmta.executeQuery(clinsqlstr);
		//	while(clinlistRS.next()) { clinlist.add(clinlistRS.getString("location_identifier_value"));}


			//fetching lists of patient
			String patsqlstr= 	" select distinct bene_mbi_id from bene_prvdr_tax_a";
			patsqlstr+=	" where bene_mbi_id in (select DISTINCT bene_mbi_id from c_c_l_f1_s ) order by bene_mbi_id";
			//	Statement stmtb = conn.createStatement();
			//	ResultSet patlistRS = stmtb.executeQuery(patsqlstr);
			//	while(patlistRS.next()) { patlist.add(patlistRS.getString("bene_mbi_id"));}


			//fetching lists of generalpractitioner
			String pracsqlstr="select distinct rndrg_prvdr_npi_num from bene_prvdr_tax_a ";
			pracsqlstr+= " where rndrg_prvdr_npi_num in (select DISTINCT atndg_prvdr_npi_num from c_c_l_f1_s )";
			pracsqlstr+=" order by rndrg_prvdr_npi_num asc";
			//Statement stmtc = conn.createStatement();
			//	ResultSet praclistRS = stmtc.executeQuery(pracsqlstr);
			//	while(praclistRS.next()) { practlist.add(praclistRS.getString("rndrg_prvdr_npi_num"));}


			String patsql= " select Distinct";
			patsql+= 	" c8.bene_1st_name,";
			patsql+= 	" c8.bene_midl_name,";
			patsql+= 	" c8.bene_last_name,";
			patsql+= 	" c8.bene_mbi_id as patient_identifier_value,";
			patsql+= "c8.bene_fips_state_cd,";
			patsql+=  "c8.bene_zip_cd,";
			patsql+=  "c8.bene_dob,";
			patsql+= 	" c8.bene_sex_cd,";
			patsql+= 	 "bene_race_cd,";
			patsql+= 	" Coalesce (bene_death_dt,'1001-01-01') bene_death_dt,";

			patsql+= 	"Trim(c8.bene_line_1_adr) ||', ' || Trim(c8.bene_line_2_adr)||', ' ||Trim(c8.bene_line_3_adr)||', ' ||Trim(c8.bene_line_4_adr)||', ' ||Trim(c8.bene_line_5_adr)||', ' ||Trim(c8.bene_line_6_adr) bene_line_1_adr,";
			patsql+= 	" c8.geo_zip_plc_name,";
			patsql+= 	 "c8.geo_usps_state_cd,";
			patsql+= "Coalesce (c9.hicn_mbi_xref_ind,'') patient_identifier_system";
			patsql+=" from c_c_l_f8_s c8 left join c_c_l_f9_s c9 on c8.bene_mbi_id = c9.bene_mbi_id  where ";
			patsql+= " c8.bene_mbi_id in ";
			patsql+= " ( select DISTINCT bene_mbi_id from c_c_l_f5_s ";

//					 patsql+= " where rndrg_prvdr_npi_num= )";
//					 patsql+= " and bene_mbi_id=";

			//fetching lists of organization
			String sqlstr2="select aco_id from acos_2022 where aco_id in (select aco_id from c_c_l_f1_s order by aco_id asc) order by aco_id asc";
			Statement stmtd = conn.createStatement();
			ResultSet rset1 = stmtd.executeQuery(sqlstr2);
			while(rset1.next()) { acolist.add(rset1.getString("aco_id"));}


			String sqlprac= "select npi  as Practitioner_identifier_value, ";
			sqlprac+= "provider_last_name,";
			sqlprac+= " provider_first_name,";
			sqlprac+= "	 provider_middle_name,";
			sqlprac+= "	 provider_first_line_business_mailing_address,";
			sqlprac+= "	 provider_second_line_business_mailing_address,";
			sqlprac+= " provider_business_mailing_address_city_name,";
			sqlprac+= "	 provider_business_mailing_address_state_name,";
			sqlprac+= "	 provider_business_mailing_address_country_code,";
			sqlprac+= "	 provider_business_mailing_address_telephone_number,";
			sqlprac+= "	 provider_first_line_business_practice_location_address,";
			sqlprac+= "	 provider_business_practice_location_address_city_name,";
			sqlprac+= "	 provider_business_practice_location_address_state_name,";
			sqlprac+=	"	 provider_business_practice_location_address_postal_code,";
			sqlprac+=	 "provider_business_practice_location_address_country_code,";
			sqlprac+=	 "	 provider_business_practice_location_address_telephone_number,";
			sqlprac+=	 "	 provider_business_practice_location_address_fax_number,";
			sqlprac+=	 "	 provider_gender_code";
			sqlprac+=	 " FROM public.npidata";
			sqlprac+=	 " where";
			sqlprac+=	 " npidata.entity_type_code ='1' and ";
			//	 sqlprac+=	 "and npidata.npi = ";


			String sqlEob ="select distinct c5.aco_id as org_ident,	c5.cur_clm_uniq_id clm_ident, COALESCE(c5.clm_line_num,'00') eob_item_seq,	c5.bene_mbi_id pat_id";
			sqlEob+=	",c5.rndrg_prvdr_npi_num Eob_cartm_prov_ident_val1,";
			sqlEob+=	" c5.bene_hic_num, c5.clm_type_cd  eob_tp_cdng_code,c5.clm_from_dt Eob_billablePeriod_start, c5.clm_thru_dt Eob_billablePeriod_end,";
			sqlEob+=	" c5.rndrg_prvdr_type_cd Eob_crtm_ext_valuecdng_cd,c5.rndrg_prvdr_fips_st_cd	Eob_item_loc_cc_ext_val_cd,";
			sqlEob+=" c5.clm_prvdr_spclty_cd	Eob_ct_qual_cdng_cd,c5.clm_fed_type_srvc_cd	Eob_item_cat_cdng_cd,c5.clm_pos_cd Eob_item_loc_cc_cdng,";
			sqlEob+=" c5.clm_line_from_dt Eob_item_servper_st,	c5.clm_line_thru_dt Eob_item_servper_end,	c5.clm_line_hcpcs_cd";
			sqlEob+=" Eob_item_prdSrv_cdng_cd, COALESCE(c5.clm_line_cvrd_pd_amt,'00') Eob_item_adjud_amnt_val_1,";
			sqlEob+=		" c5.clm_line_prmry_pyr_cd Eob_item_ext_val_cdng_code1,	c5.clm_line_dgns_cd clm_dgns_0_cd,	c5.clm_rndrg_prvdr_tax_num";
			sqlEob+=" Eob_item_ext_val_cdng_code2,c5.clm_carr_pmt_dnl_cd Eob_ext_val_cdng_code,";
			sqlEob+=	" c5.clm_prcsg_ind_cd Eob_item_ext_val_cdng_code3,	c5.clm_adjsmt_type_cd Eob_status,	c5.clm_efctv_dt Eob_sI_tim_Dt,	c5.clm_idr_ld_dt,";
			sqlEob+=	" c5.clm_cntl_num Eob_ident_val,	c5.bene_eqtbl_bic_hicn_num,COALESCE(c5.clm_line_alowd_chrg_amt,'00') Eob_item_adjud_amnt_val_2,";
			sqlEob+=	" COALESCE(c5.clm_line_srvc_unit_qty,'00') Eob_item_quant_val,	c5.hcpcs_1_mdfr_cd Eob_item_mod_cdng_cd_1";
			sqlEob+=	",c5.hcpcs_2_mdfr_cd	Eob_item_mod_cdng_cd_2 , c5.hcpcs_3_mdfr_cd Eob_item_mod_cdng_cd_3,";
			sqlEob+=	"c5.hcpcs_4_mdfr_cd Eob_item_mod_cdng_cd_4,	c5.hcpcs_5_mdfr_cd Eob_item_mod_cdng_cd_5,	c5.clm_disp_cd Eob_disposition,";
			sqlEob+=	"c5.clm_dgns_1_cd ,	c5.clm_dgns_2_cd,	c5.clm_dgns_3_cd,	c5.clm_dgns_4_cd,	c5.clm_dgns_5_cd,";
			sqlEob+=	"c5.clm_dgns_6_cd,	c5.clm_dgns_7_cd,	c5.clm_dgns_8_cd,	c5.dgns_prcdr_icd_ind,	c5.clm_dgns_9_cd,	c5.clm_dgns_10_cd,";
			sqlEob+=	" c5.clm_dgns_11_cd,	c5.clm_dgns_12_cd, c5.hcpcs_betos_cd Eob_item_ext_val_cdng_code4,";

			sqlEob+=	"c6.aco_id,	c6.cur_clm_uniq_id,	COALESCE(c6.clm_line_num,'00')  eob_item_seq_2,	c6.bene_mbi_id,	c6.bene_hic_num,	c6.clm_type_cd eob_type_cdng_cd,	c6.clm_from_dt eob_billper_st,	c6.clm_thru_dt eob_billper_en,";
				sqlEob+=	"c6.clm_fed_type_srvc_cd eob_item_cat_cdng_cd_2,	c6.clm_pos_cd Eob_item_location_cc_cdng,	c6.clm_line_from_dt eob_item_servper_st,	c6.clm_line_thru_dt Eob_item_servper_en,";
				sqlEob+=	"c6.clm_line_hcpcs_cd Eob_item_prdSrv_cdng_cd_2,";
				sqlEob+=	"COALESCE(c6.clm_line_cvrd_pd_amt,'00')  eob_item_adjud_amnt_val_3, c6.clm_prmry_pyr_cd eob_item_ext_valcdng_cd_2 ,c6.payto_prvdr_npi_num Eob_cartm_prov_ident_val2,	c6.ordrg_prvdr_npi_num Eob_cartm_prov_ident_val3,";
				sqlEob+=	"c6.clm_carr_pmt_dnl_cd eob_item_exten_val_cdng_cd_3,	c6.clm_prcsg_ind_cd Eob_item_ext_val_cdng_code5,	c6.clm_adjsmt_type_cd eob_status_2,	c6.clm_efctv_dt eob_si_timedt_2,	c6.clm_idr_ld_dt,";
				sqlEob+=	"c6.clm_cntl_num Eob_ident_val_clm_cntrl_num_2,";
				sqlEob+=	"c6.bene_eqtbl_bic_hicn_num , COALESCE(c6.clm_line_alowd_chrg_amt,'00') Eob_item_adjud_amnt_val_4,	c6.clm_disp_cd	Eob_disposition_2,";
			sqlEob+=	" c7.aco_id org_ident,	c7.	cur_clm_uniq_id clm_ident,	c7.	bene_mbi_id	pat_ident,	c7.	clm_line_ndc_cd	Eob_item_prdSrv_cdng_cd_3,";
			sqlEob+=	" c7. clm_type_cd eob_type_cdng_cd2, c7.clm_line_from_dt Eob_item_servper_st, c7.clm_srvc_prvdr_gnrc_id_num eob_ct_prov_gern_id,";
			sqlEob+=	" c7.	clm_dspnsng_stus_cd, c7.clm_daw_prod_slctn_cd Eob_si_cdng_cd_cd,	COALESCE(c7.clm_line_srvc_unit_qty,'00') Eob_item_quant_val3 ,";
			sqlEob+=	" COALESCE(c7.clm_line_days_suply_qty,'00')  eob_si_val_q_val,	 c7.clm_prsbng_prvdr_gnrc_id_num eob_fac_ident_val,";
			sqlEob+=	" COALESCE(c7.clm_line_bene_pmt_amt,'00')  eob_item_adjud_amnt_val_5,	c7.clm_adjsmt_type_cd	eob_status_3,";
			sqlEob+=	" c7.clm_efctv_dt Eob_si_tim_dt_3, c7.clm_line_rx_srvc_rfrnc_num eob_ident_val_presc_serv";
			sqlEob+=	", COALESCE(c7.clm_line_rx_fill_num,'00') eob_sivalq_val, COALESCE(c7.clm_phrmcy_srvc_type_cd,'00')	eob_fac_ext_valcdng_code";
			sqlEob+=" from c_c_l_f5_s c5  left join c_c_l_f6_s c6 on c5.bene_mbi_id=c6.bene_mbi_id left join c_c_l_f7_s c7 on";
			sqlEob+=" c5.bene_mbi_id=c7.bene_mbi_id where ";
		//c5.aco_id='A3102' and c5.bene_mbi_id='8AV0EE5YF00'
		//	and c5.cur_clm_uniq_id='0085595668435' and c5.rndrg_prvdr_npi_num='1639201239' ;
			//	sqlEob2	+= 	" from c_c_l_f2_s c2 where";


			//	 bene_mbi_id ='4AX0V51KF31' and cur_clm_uniq_id='0067126075589'


//			String sqlclinic=	 "select  npi as  location_identifier_value,  provider_organization_name,";
//			sqlclinic+=	 "provider_business_mailing_address_telephone_number,";
//			sqlclinic+=" provider_business_practice_location_address_telephone_number,";
//			sqlclinic+=	" provider_first_line_business_mailing_address,";
//			sqlclinic+=	" provider_second_line_business_mailing_address, ";
//			sqlclinic+= "provider_business_mailing_address_city_name,";
//			sqlclinic+=		" provider_business_mailing_address_state_name,";
//			sqlclinic+=		" provider_business_mailing_address_country_code,";
//			sqlclinic+=		" provider_business_mailing_address_telephone_number,";
//			sqlclinic+=		" provider_first_line_business_practice_location_address,";
//			sqlclinic+=		" provider_business_practice_location_address_city_name,";
//			sqlclinic+=		" provider_business_practice_location_address_state_name,";
//			sqlclinic+=		" provider_business_practice_location_address_postal_code,";
//			sqlclinic+=		" provider_business_practice_location_address_country_code,";
//			sqlclinic+=		 "provider_business_practice_location_address_telephone_number,";
//			sqlclinic+=		" provider_business_practice_location_address_fax_number ";
//			sqlclinic+=	 "FROM public.npidata";
//			sqlclinic+=	" where ";
//			sqlclinic+= " npidata.entity_type_code ='2' ";
//			sqlclinic+= " and npidata.npi in (select distinct co.fac_prvdr_npi_num from public.c_c_l_f1_s co ";
//			// sqlclinic+="where co.aco_id='A3886') and npidata.npi=";
//
//			//query for organization
			String sqlstr ="select aco_id organization_identifier_value,";
			sqlstr +=	" aco_exec_phone  as organization_contact_telecom,";
			sqlstr +=	" aco_address   as organization_addressline,";
			sqlstr += " aco_exec_email   as organization_contact_email,";
			sqlstr +=	"aco_exec_name 	  as organization_contact_name,";
			sqlstr += " aco_public_name   as organization_name,";
			sqlstr +=	" aco_public_email, aco_public_phone  from acos_2022";
			sqlstr += " where aco_id in (Select distinct aco_id from c_c_l_f5_s)";
			sqlstr +="union";
			sqlstr += " select aco_id organization_identifier_value,";
			sqlstr += " aco_exec_phone  as organization_contact_telecom,";
			sqlstr +=	" aco_address   as organization_addressline,";
			sqlstr += " aco_exec_email   as organization_contact_email,";
			sqlstr +=	" aco_exec_name 	  as organization_contact_name,";
			sqlstr += " aco_public_name   as organization_name,";
			sqlstr +=	" aco_public_email, aco_public_phone  from acos_2019";
			sqlstr += " where aco_id in (Select distinct aco_id from c_c_l_f5_s)";

//			for (int i = 0; i < acolist.size(); i++)
//			{
//				String sql = sqlstr+" '"+acolist.get(i).toString()+"'";
//				System.out.println(" organization SQL : " + sql + "\n");


			Statement stmt2 = conn.createStatement();
				ResultSet rset = stmt2.executeQuery(sqlstr);
			org.hl7.fhir.r4.model.ContactPoint cp = new org.hl7.fhir.r4.model.ContactPoint();
			cp.setRank(1);
			Organization.OrganizationContactComponent occ = new Organization.OrganizationContactComponent();
			org.hl7.fhir.r4.model.HumanName contactname = new  org.hl7.fhir.r4.model.HumanName();
			org.hl7.fhir.r4.model.StringType strname = new  org.hl7.fhir.r4.model.StringType();
			org.hl7.fhir.r4.model.Address address = new  org.hl7.fhir.r4.model.Address();
			Organization organization = new Organization();
				while(rset.next()) {

						organization.clearUserData(orgname);
						organization.setMeta(clmeta);
						organization.addIdentifier().setValue(rset.getString("organization_identifier_value"));
						String orgident = rset.getString("organization_identifier_value");


			FhirApiReq apiCall2= new FhirApiReq();
			//	boolean orgbool= apiCall2.ApiGet(orgident,"Organization");
			boolean orgbool= false;
			//	boolean orgbool= apiCall2.ApiGet("A3886","Organization");

			if (orgbool == true) {
				//orgid=apiCall2.ApiGetid(orgident, "Organization");
				//		orgid=apiCall2.ApiGetid("A3886", "Organization");
			}
			if (orgbool == false) {
						organization.setName(rset.getString("organization_name"));
						orgname = rset.getString("organization_name");
						cp.setValue(rset.getString("organization_contact_telecom"));
						//organization.setTelecom(Arrays.asList(cp));
						address.addLine(rset.getString("organization_addressline"));
						organization.addAddress(address);
						strname.setValue(rset.getString("organization_contact_name"));


				contactname.setGiven(Arrays.asList(strname));
				occ.addTelecom(cp);
				occ.setName(contactname);
				organization.addContact(occ);
				serOrg = jsonparser.encodeResourceToString(organization);
				System.out.println(serOrg);
				FhirApiReq far1 = new FhirApiReq();
				//		far.ApiPost
	//			orgid = far1.ApiPost(serOrg, "Organization");
			}

			//		for (int j = 9; j < clinlist.size(); j++) {
//						sql = sqlclinic + " where co.aco_id='" + orgident + "') and npidata.npi=" + " '" + clinlist.get(j).toString() + "'";
//						String clinicident =clinlist.get(j).toString();
//						System.out.println("location clinic SQL: " + sql + "\n");
//						//	Connection conntop11 = DriverManager.getConnection("jdbc:postgresql://0.000.000.000:5432/cclfdb","postgres", "password");
//						//	cliniccount++;
//						Statement stmt3 = conn.createStatement();
//						ResultSet rsetclin = stmt3.executeQuery(sql);
//						org.hl7.fhir.r4.model.ContactPoint cp1 = new org.hl7.fhir.r4.model.ContactPoint();
//						cp1.setRank(1);
//						org.hl7.fhir.r4.model.Address address1 = new org.hl7.fhir.r4.model.Address();
//						org.hl7.fhir.r4.model.Location location = new org.hl7.fhir.r4.model.Location();
//						org.hl7.fhir.r4.model.Reference reference = new org.hl7.fhir.r4.model.Reference();
//						FhirApiReq apiCall= new FhirApiReq();
//						boolean clinicbool = false;
//						//		boolean clinicbool= apiCall.ApiGet(clinicident,"Location");
//						String clinicid="";
//
//						try{	while (rsetclin.next()) {
//							if (clinicbool == true) {
//								//	clinicid=apiCall.ApiGetid(clinicident, "Location");
//							}
//
//			if (clinicbool == false) {
//				cliniccount++;
//				location.setMeta(clmeta);
//				location.setStatus(LocationStatus.ACTIVE);
//				location.addIdentifier().setValue(rsetclin.getString("location_identifier_value"));
//				String clinIdent = rsetclin.getString("location_identifier_value");
//				location.setName(rsetclin.getString("provider_organization_name"));
//				cp.setValue(rsetclin.getString("provider_business_practice_location_address_telephone_number"));
//				address.addLine(rsetclin.getString("provider_first_line_business_practice_location_address"));
//				address.setCity(rsetclin.getString("provider_business_practice_location_address_city_name"));
//				address.setState(rsetclin.getString("provider_business_practice_location_address_state_name"));
//				address.setPostalCode(rsetclin.getString("provider_business_practice_location_address_postal_code"));
//				location.addTelecom(cp);
//				location.setAddress(address1);
//				reference.setReference("Organization/" + orgid);
//				reference.setDisplay(orgname);
//				location.setManagingOrganization(reference);
//				serOrg = jsonparser.encodeResourceToString(location);
//				System.out.println(serOrg);
//				//	ApiPost(serOrg, "Location");
//			}//end of clinic for loop

			//praclist loop
			//for (i = 307; i < practlist.size(); i++) // 307 mark at 12:06 am 08/03/22
			//	for ( i = 0; i < 5; i++)
			//	{
			//sql = sqlprac + " '" + practlist.get(i).toString() + "'";
			//sql = sqlprac + " npidata.npi in (Select atndg_prvdr_npi_num from c_c_l_f1_s where fac_prvdr_npi_num ='" + clinicident + "')";
			String 	sql = sqlprac + " npidata.npi in (Select rndrg_prvdr_npi_num from c_c_l_f5_s where aco_id='A3102' )";

			System.out.println("The practitioner sql : " + sql + "\n");
			Statement stmt4 = conn.createStatement();
			ResultSet rsetprac = stmt4.executeQuery(sql);

			try {
			while (rsetprac.next()) {
			org.hl7.fhir.r4.model.ContactPoint cp2 = new org.hl7.fhir.r4.model.ContactPoint();
			org.hl7.fhir.r4.model.Address address2 = new org.hl7.fhir.r4.model.Address();
			Practitioner practitioner = new Practitioner();
			org.hl7.fhir.r4.model.HumanName contactname2 = new org.hl7.fhir.r4.model.HumanName();
			//clinIdent
			String pracid = "0";
			String pname = "";
			String pracnpi = rsetprac.getString("Practitioner_identifier_value");
			boolean testident =false;
			 cp = new org.hl7.fhir.r4.model.ContactPoint();
				FhirApiReq apiCall= new FhirApiReq();
			//	 testident = apiCall.ApiGet(pracnpi, "Practitioner");

			if (testident == true) {
			//		pracid=apiCall.ApiGetid(pracnpi, "Patient");
			}

			if (testident == false) {
				praccount++;
				practitioner.setActive(true);
				cp2.setRank(1);
				practitioner.addIdentifier().setValue(rsetprac.getString("Practitioner_identifier_value"));
				pname = rsetprac.getString("provider_first_name") + " " + rsetprac.getString("provider_middle_name") + " " + rsetprac.getString("provider_last_name");
				contactname2.addGiven(pname);
				practitioner.addName(contactname2);

				cp.setValue(rsetprac.getString("provider_business_practice_location_address_telephone_number"));
				address2.addLine(rsetprac.getString("provider_first_line_business_practice_location_address"));
				address2.setCity(rsetprac.getString("provider_business_practice_location_address_city_name"));
				address2.setState(rsetprac.getString("provider_business_practice_location_address_state_name"));
				address2.setPostalCode(rsetprac.getString("provider_business_practice_location_address_postal_code"));
				practitioner.addTelecom(cp);
				practitioner.setAddress(Arrays.asList(address2));
				practitioner.setMeta(clmeta);

				 serOrg  = jsonparser.encodeResourceToString(practitioner);
				System.out.println(serOrg);
				//	pracid = ApiPost(serOrg, "Practitioner");
			}//end of prac if condition


			//	for (i = 0; i < 1; i++) {
			//sql = patsql + " where atndg_prvdr_npi_num= '" + pracnpi + "' )" + " and bene_mbi_id=" + " '" + patlist.get(i).toString() + "'";
			sql = patsql + " where rndrg_prvdr_npi_num= '" + pracnpi + "' ) order by c8.bene_mbi_id asc";
			System.out.println("The patient SQL is: " + sql + "");
			Statement stmt5 = conn.createStatement();
			ResultSet rsetpat = stmt5.executeQuery(sql);
			ResultSetMetaData rsmd = rsetpat.getMetaData();
			int columnsNumber = rsmd.getColumnCount();
			//Date date2 = new Date();
			//date2.
			//			String sDate1="31/12/1998";
			//Date date1=new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
			try {
			String patname = "";
			while (rsetpat.next()) {

			org.hl7.fhir.r4.model.ContactPoint cp3 = new org.hl7.fhir.r4.model.ContactPoint();
			org.hl7.fhir.r4.model.Address address3 = new org.hl7.fhir.r4.model.Address();
			Patient patient1 = new Patient();
			org.hl7.fhir.r4.model.HumanName patientName = new org.hl7.fhir.r4.model.HumanName();
			org.hl7.fhir.r4.model.Reference pracreference = new org.hl7.fhir.r4.model.Reference();
			org.hl7.fhir.r4.model.Reference orgreference = new org.hl7.fhir.r4.model.Reference();

			String patident = rsetpat.getString("Patient_identifier_value");
			FhirApiReq getidCall = new FhirApiReq();
			boolean testpatident =false;
		//	 testpatident = getidCall.ApiGet(patident, "Patient");

			String patid = "1";

			if (testpatident == true) {
			//		patid=getidCall.ApiGetid(patident, "Patient");
			}
			if (testpatident == false) {
				patcount++;
				cp3.setRank(1);
				patient1.setMeta(clmeta);
				patient1.setActive(true);
				String patsys= rsetpat.getString("patient_identifier_system");
				patient1.addIdentifier().setSystem(patsys.trim())
						.setValue(rsetpat.getString("Patient_identifier_value"));

				String patfirst = rsetpat.getString("bene_1st_name").trim();
				String patmid = rsetpat.getString("bene_midl_name").trim();
				String patlast = rsetpat.getString("bene_midl_name").trim();
				patname = patfirst + " " + patmid + " " + patlast;
				patientName.addGiven(patname);
				patientName.setFamily(rsetpat.getString("bene_last_name").trim());
				if (patlast == null || patlast.isEmpty() || patlast == "") {
					patientName.setUse(org.hl7.fhir.r4.model.HumanName.NameUse.USUAL);
				} else {
					patientName.setUse(org.hl7.fhir.r4.model.HumanName.NameUse.OFFICIAL);
				}
				patient1.addName(patientName);
				//cp.setValue(rsetpat.getString("provider_business_practice_location_address_telephone_number"));
				address3.addLine(rsetpat.getString("bene_line_1_adr").trim());
				address3.setCity(rsetpat.getString("geo_zip_plc_name").trim());//GEO_ZIP_PLC_NAME//

				address3.setState(rsetpat.getString("bene_fips_state_cd").trim());//BENE_FIPS_STATE_CD//previously geo_usps_state_cd

				address3.setPostalCode(rsetpat.getString("bene_zip_cd").trim());//bene_zip_cd
				String sDate1 = rsetpat.getString("bene_dob").trim();
				Date date1 = new SimpleDateFormat("yyyy-mm-dd").parse(sDate1);
				patient1.setBirthDate(date1);
				String sex_cd = rsetpat.getString("bene_sex_cd").trim();
				if (sex_cd.equals("1")) {
					sex_cd = "male";
				} else {
					sex_cd = "female";
				}
				String patdecdate = rsetpat.getString("bene_death_dt").trim();
				org.hl7.fhir.r4.model.DateType datetype = new org.hl7.fhir.r4.model.DateType();
				Date perdate = new SimpleDateFormat("yyyy-mm-dd").parse(patdecdate);

				datetype.setValue(perdate);
				Type patdeceased = new org.hl7.fhir.r4.model.BooleanType();
				if (patdecdate != "1001-01-01")
				{
					patdeceased.setId("True");
					patient1.setDeceased(patdeceased);
				}
				//	patient1.setDeceased(datetype);
				List<org.hl7.fhir.r4.model.Extension> patextL = new ArrayList<org.hl7.fhir.r4.model.Extension>();
				org.hl7.fhir.r4.model.Extension patext = new org.hl7.fhir.r4.model.Extension();
				Type patraceval = new org.hl7.fhir.r4.model.CodeableConcept();
				patraceval.setId(rsetpat.getString("bene_race_cd").trim());//c8.bene_race_cd

				patext.setId("bene_race_cd");//c8.bene_race_cd
				patext.setValue(patraceval);
				patextL.add(patext);
				patient1.setExtension(patextL);
				patient1.setGender(Enumerations.AdministrativeGender.fromCode(sex_cd));
				patient1.addAddress(address2);
				pracreference.setReference("Practitioner/" + pracid);
				pracreference.setDisplay(pname);
				orgreference.setReference("Organization/" + orgid);
				orgreference.setDisplay(orgname);
				patient1.setManagingOrganization(orgreference);
				patient1.addGeneralPractitioner(pracreference);
				serOrg = jsonparser.encodeResourceToString(patient1);
				System.out.println(serOrg);
				//patid = ApiPost(serOrg, "Patient");
			}//end of if condition for patient if it alrady exists

			/// for nested claim
			sql = "select distinct c1.bene_mbi_id patient, c8.BENE_ORGNL_ENTLMT_RSN_CD cov_ext1, BENE_ENTLMT_BUYIN_IND cov_ext2,";
			sql +=	"rndrg_prvdr_npi_num doc_npi, cur_clm_uniq_id Claim_Identifier  ,c8.bene_mdcr_stus_cd";
			sql +=" Cov_ext_valcd, c8.bene_dual_stus_cd Cov_ext_valcd2, c8.eligibility_status exten_id ,COALESCE";
			sql +=	"(c8.bene_part_a_enrlmt_bgn_dt,'1001-01-01')";
			sql +=	"cov_start_dt_a,c8.bene_part_b_enrlmt_bgn_dt cov_start_dt_b  from c_c_l_f5_s c1 left join c_c_l_f8_s c8";
			sql +="  on c1.bene_mbi_id=c8.bene_mbi_id ";
			sql += " where c1.bene_mbi_id ='" + patident + "' ";
			sql += " and c1.rndrg_prvdr_npi_num ='" + pracnpi + "'  order by cur_clm_uniq_id asc";
			System.out.println("The claim SQL is: " + sql + "");
			Statement stmt6 = conn.createStatement();
			ResultSet rsetclaim = stmt6.executeQuery(sql);
			//Beneficiary Medicare Status Code BENE_MDCR_STUS_CD
			//BENE_DUAL_STUS_CD	Beneficiary Dual Status Code
			try {
				while (rsetclaim.next()) {
					String claimIdent = rsetclaim.getString("Claim_Identifier");
					String coverageIdent = rsetclaim.getString("Claim_Identifier");
					FhirApiReq getclmidCall = new FhirApiReq();
					boolean testclmident = false;
					//	 testclmident = getclmidCall.ApiGet(claimIdent, "Claim");
					String claimid = "1";
					String coverageid= "";

					if (testclmident == true) {
						//		claimid=getidCall.ApiGetid(claimIdent, "Claim");
						coverageid=claimid;
					}

			if (testclmident == false) {
				claimcount++;
				Claim claim = new Claim();
				Coverage coverage = new Coverage();
				org.hl7.fhir.r4.model.Contract contract = new org.hl7.fhir.r4.model.Contract();
				contract.addIdentifier().setValue(rsetclaim.getString("Claim_Identifier"));
				contract.addAlias("coverage/part-a");

				claim.setMeta(clmeta);
				coverage.setMeta(clmeta);

				claim.addIdentifier().setValue(rsetclaim.getString("Claim_Identifier"));
				coverage.addIdentifier().setValue(rsetclaim.getString("Claim_Identifier"));
				coverage.setStatus(Coverage.CoverageStatus.ACTIVE);
				claim.setStatus(ClaimStatus.ACTIVE);
				org.hl7.fhir.r4.model.Reference patientReference = new org.hl7.fhir.r4.model.Reference();
				org.hl7.fhir.r4.model.Reference providerreference = new org.hl7.fhir.r4.model.Reference();
				providerreference.setReference("Practitioner/" + pracid.trim());
				providerreference.setDisplay(pname);
				patientReference.setReference("Patient/" + patid.trim());
				patientReference.setDisplay(patname);
				claim.setPatient(patientReference);
				claim.setProvider(providerreference);
				coverage.setBeneficiary(patientReference);
				org.hl7.fhir.r4.model.CodeableConcept covrelcode= new org.hl7.fhir.r4.model.CodeableConcept();
				org.hl7.fhir.r4.model.Coding relcoding = new org.hl7.fhir.r4.model.Coding();
				relcoding.setDisplay("Self");
				covrelcode.addCoding(relcoding);
				Coverage.ClassComponent covcc= new Coverage.ClassComponent();
				org.hl7.fhir.r4.model.Reference conref = new org.hl7.fhir.r4.model.Reference();
				org.hl7.fhir.r4.model.Reference payorref = new org.hl7.fhir.r4.model.Reference();
				payorref.setDisplay("Centers for Medicare and Medicaid Services");
				//	conref.setReference();
				conref.setDisplay("coverage/part-a");
				covcc.setValue("Part_A");

				org.hl7.fhir.r4.model.Period covperiod = new org.hl7.fhir.r4.model.Period();
				Date perdate = new SimpleDateFormat("yyyy-mm-dd").parse(rsetclaim.getString("cov_start_dt_a"));
				covperiod.setStart(perdate);
				coverage.setPeriod(covperiod);


				org.hl7.fhir.r4.model.Extension covext = new org.hl7.fhir.r4.model.Extension();
				org.hl7.fhir.r4.model.Extension covext2 = new org.hl7.fhir.r4.model.Extension();
				org.hl7.fhir.r4.model.Extension covext3 = new org.hl7.fhir.r4.model.Extension();
				org.hl7.fhir.r4.model.Extension covext4 = new org.hl7.fhir.r4.model.Extension();

				List<org.hl7.fhir.r4.model.Extension> covextl = new ArrayList<org.hl7.fhir.r4.model.Extension>();
				Type covextval = new org.hl7.fhir.r4.model.CodeableConcept();
				Type covextval2 = new org.hl7.fhir.r4.model.CodeableConcept();
				Type covextval3 = new org.hl7.fhir.r4.model.CodeableConcept();
				Type covextval4 = new org.hl7.fhir.r4.model.CodeableConcept();

				covextval.setId(rsetclaim.getString("Cov_ext_valcd"));
				covext.setValue(covextval);
				covext.setId(rsetclaim.getString("exten_id"));
				covextl.add(covext);

				covextval2.setId(rsetclaim.getString("Cov_ext_valcd2"));
				covext2.setId("Beneficiary Dual Status Code");
				covext2.setValue(covextval2);
				covextl.add(covext2);
				covextval3.setId(rsetclaim.getString("cov_ext1"));
				covext3.setId("Beneficiary Original Entitlement Reason Code");
				covext3.setValue(covextval3);
				covextl.add(covext3);
				covextval4.setId(rsetclaim.getString("cov_ext2"));
				covext4.setId("Beneficiary Entitlement Buy-in Indicator");
				covext4.setValue(covextval4);
				covextl.add(covext4);
				sql="SELECT ";
				sql+= "round(count(c_c_l_f8_s.bene_mbi_id )*1.0 /12,3) as person_years,";
				sql+= " c_c_l_f8_s.bene_mbi_id as mbi, c_c_l_f8_s.year as cov_year";
				sql+= " FROM c_c_l_f8_s";
				sql+= " WHERE";
				sql+= " c_c_l_f8_s.bene_mbi_id ='" + patident + "'";
				sql+= " AND runout = FALSE";
				sql+= " AND c_c_l_f8_s.bene_entlmt_buyin_ind in('C', '3')";
				sql+=" GROUP BY ";
				sql+=" c_c_l_f8_s.bene_mbi_id,c_c_l_f8_s.year";
				Statement newstmt9 = conn.createStatement();
				ResultSet rsetcov = newstmt9.executeQuery(sql);
				String covyr,persyr= "";

				try{
					while (rsetcov.next())
					{
						org.hl7.fhir.r4.model.Extension covextcalc = new org.hl7.fhir.r4.model.Extension();
						Type covextval7 = new org.hl7.fhir.r4.model.CodeableConcept();
						covyr="";
						persyr= "";
						covyr= rsetcov.getString("cov_year");
						covextcalc.setId("Person_Years - "+covyr);
						persyr=rsetcov.getString("person_years");
						covextval7.setId(persyr);
						covextcalc.setValue(covextval7);
						covextl.add(covextcalc);
					}

				}
				catch(Exception ex )
				{
					System.out.println(ex.getMessage());

				}

				coverage.addPayor(payorref);
				coverage.setRelationship(covrelcode);
				coverage.addContract(conref);
				coverage.addClass_(covcc);

				coverage.setExtension(covextl);

				//coverage.addContract(conref);

				serOrg = jsonparser.encodeResourceToString(claim);
				System.out.println(serOrg);
				String serOrg2 = jsonparser.encodeResourceToString(coverage);
				System.out.println(serOrg2);
				//	claimid = ApiPost(serOrg, "Claim");
				//	coverageid= ApiPost(serOrg2, "Coverage");


				sql = sqlEob + " c5.aco_id='A3102' and c5.bene_mbi_id='" + patident + "'   ";
				sql +=	"and c5.cur_clm_uniq_id='" + claimIdent + "' and c5.rndrg_prvdr_npi_num='" + pracnpi + "' order by  cur_clm_uniq_id asc" ;

				System.out.println("The EOB SQL is: " + sql + "");
				Statement stmt7 = conn.createStatement();
				ResultSet rseteob = stmt7.executeQuery(sql);
				ExplanationOfBenefit eob = new ExplanationOfBenefit();

//				org.hl7.fhir.r4.model.CodeableConcept eobprccd = new org.hl7.fhir.r4.model.CodeableConcept();
//				List<ExplanationOfBenefit.ProcedureComponent> pcL = new ArrayList <ExplanationOfBenefit.ProcedureComponent>();
//				List<ExplanationOfBenefit.DiagnosisComponent> ldc = new ArrayList<>();
//				List<ExplanationOfBenefit.ItemComponent> ItemsL = new ArrayList<ExplanationOfBenefit.ItemComponent>();
//				ExplanationOfBenefit.SupportingInformationComponent sic = new ExplanationOfBenefit.SupportingInformationComponent();
//				ExplanationOfBenefit.InsuranceComponent insurC= new ExplanationOfBenefit.InsuranceComponent();
//				ExplanationOfBenefit.TotalComponent totAmtCmp = new ExplanationOfBenefit.TotalComponent();
//				org.hl7.fhir.r4.model.CodeableConcept etcco = new org.hl7.fhir.r4.model.CodeableConcept();
//				ExplanationOfBenefit.ProcedureComponent eobprcc = new ExplanationOfBenefit.ProcedureComponent();
//				List<org.hl7.fhir.r4.model.CodeableConcept> procL = new ArrayList<org.hl7.fhir.r4.model.CodeableConcept>();
//				org.hl7.fhir.r4.model.Coding prccoding = new org.hl7.fhir.r4.model.Coding();
//				List<org.hl7.fhir.r4.model.CodeableConcept> diagtpL = new ArrayList<org.hl7.fhir.r4.model.CodeableConcept>();
//				org.hl7.fhir.r4.model.CodeableConcept prccd = new org.hl7.fhir.r4.model.CodeableConcept();
//				List<org.hl7.fhir.r4.model.CodeableConcept> eobprctpL = new ArrayList<org.hl7.fhir.r4.model.CodeableConcept>();
//				List<org.hl7.fhir.r4.model.Coding> eobcodingtpL = new ArrayList<org.hl7.fhir.r4.model.Coding>();
//				ExplanationOfBenefit.DiagnosisComponent diagnosis = new ExplanationOfBenefit.DiagnosisComponent();
//				ExplanationOfBenefit.DiagnosisComponent prdiagnosis = new ExplanationOfBenefit.DiagnosisComponent();
//				ExplanationOfBenefit.DiagnosisComponent addiagnosis = new ExplanationOfBenefit.DiagnosisComponent();
//				org.hl7.fhir.r4.model.Coding coding = new org.hl7.fhir.r4.model.Coding();

				//part B
				List<ExplanationOfBenefit.ItemComponent> theiteml = new ArrayList<>();
				ExplanationOfBenefit.ItemComponent itemc = new ExplanationOfBenefit.ItemComponent();
				ExplanationOfBenefit.ItemComponent itemc2 = new ExplanationOfBenefit.ItemComponent();
				ExplanationOfBenefit.ItemComponent itemc3 = new ExplanationOfBenefit.ItemComponent();
				List<Extension> eobextl= new ArrayList<>();
				List <AdjudicationComponent> acompl = new ArrayList<>();
				List <AdjudicationComponent> acompl2 = new ArrayList<>();
				List <AdjudicationComponent> acompl3 = new ArrayList<>();
				AdjudicationComponent adjudiitem11 = new AdjudicationComponent();
				AdjudicationComponent adjudiitem12 = new AdjudicationComponent();
				AdjudicationComponent adjudiitem21 = new AdjudicationComponent();
				AdjudicationComponent adjudiitem22 = new AdjudicationComponent();
				AdjudicationComponent adjudiitem31 = new AdjudicationComponent();

				Coding extcdng1= new Coding();
				Coding extcdng2= new Coding();
				Coding extcdng3= new Coding();
				Coding extcdng4= new Coding();
				Coding extcdng5= new Coding();
				Extension eobext1 = new Extension();
				Extension eobext2 = new Extension();
				Extension eobext3 = new Extension();
				Extension eobext4 = new Extension();
				Extension eobext5 = new Extension();
				CodeableConcept modcc1= new CodeableConcept();
				CodeableConcept modcc2= new CodeableConcept();
				CodeableConcept modcc3= new CodeableConcept();
				CodeableConcept modcc4= new CodeableConcept();
				CodeableConcept modcc5= new CodeableConcept();
				String c3clm_prcdr_cd ="";
				List <CodeableConcept> modccl= new ArrayList<>();
				Coding modcdng1= new Coding();
				Coding modcdng2= new Coding();
				Coding modcdng3= new Coding();
				Coding modcdng4= new Coding();
				Coding modcdng5= new Coding();
				List<Coding> tpcdngl = new ArrayList<>();
				Coding tpcdng = new Coding();
				Coding tpcdng2 = new Coding();
				CodeableConcept eobtypecc= new CodeableConcept();

				Coding cdng = new Coding();
				Coding cdng2 = new Coding();
				Coding cdng3 = new Coding();

				CodeableConcept pserv= new CodeableConcept();
				CodeableConcept pserv2= new CodeableConcept();
				List<CodeableConcept> pserv1l= new ArrayList<>();
				List<CodeableConcept> pserv2l= new ArrayList<>();
				List<CodeableConcept> pserv3l= new ArrayList<>();
				List<DiagnosisComponent> diagl = new ArrayList<>();
				List<SupportingInformationComponent> sicl= new ArrayList<>();

				CodeableConcept pserv3= new CodeableConcept();
				try {
					while (rseteob.next()) {
						try {   //within while iteration
//							List<org.hl7.fhir.r4.model.Extension> billextL = new ArrayList<org.hl7.fhir.r4.model.Extension>();
//							List<org.hl7.fhir.r4.model.Coding> sicl = new ArrayList<org.hl7.fhir.r4.model.Coding>();
//							List<org.hl7.fhir.r4.model.Coding> eobtpl = new ArrayList<org.hl7.fhir.r4.model.Coding>();
//							List<BenefitBalanceComponent> bbL = new ArrayList<BenefitBalanceComponent>();
//							List<org.hl7.fhir.r4.model.Coding> modlist = new ArrayList<org.hl7.fhir.r4.model.Coding>();
							List<CareTeamComponent> ctcl = new ArrayList<>();
							List<Identifier> eobidentl= new ArrayList<>();


							boolean hasdaw=false;

							eobcount++;
     						org.hl7.fhir.r4.model.Reference patientReference2 = new org.hl7.fhir.r4.model.Reference();
							org.hl7.fhir.r4.model.Reference providerreference2 = new org.hl7.fhir.r4.model.Reference();
//							String eobisps = "";
//							String eobispe = "";
//							String eobisd = "";

							String eobitemseq = rseteob.getString("eob_item_seq");
							String eobitemseq2 = rseteob.getString("eob_item_seq_2");
							String eobilocccextvalcd = rseteob.getString("Eob_item_loc_cc_ext_val_cd");
							String Eob_item_cat_cdng_cd = rseteob.getString("Eob_item_cat_cdng_cd");
							String eob_item_cat_cdng_cd_2 = rseteob.getString("eob_item_cat_cdng_cd_2");

							String Eob_item_loc_cc_cdng = rseteob.getString("Eob_item_loc_cc_cdng");
							String Eob_item_servper_st = rseteob.getString("Eob_item_servper_st");
							String Eob_item_servper_end = rseteob.getString("Eob_item_servper_end");
							String Eob_item_adjud_amnt_val_1 = rseteob.getString("Eob_item_adjud_amnt_val_1");
							String Eob_item_adjud_amnt_val_2 = rseteob.getString("Eob_item_adjud_amnt_val_2");
							String Eob_item_adjud_amnt_val_3 = rseteob.getString("Eob_item_adjud_amnt_val_3");
							String Eob_item_adjud_amnt_val_4 = rseteob.getString("Eob_item_adjud_amnt_val_4");
							String Eob_item_adjud_amnt_val_5 = rseteob.getString("Eob_item_adjud_amnt_val_5");
							String Eob_item_prdSrv_cdng_cd = rseteob.getString("Eob_item_prdSrv_cdng_cd");
							String Eob_item_prdSrv_cdng_cd_2 = rseteob.getString("Eob_item_prdSrv_cdng_cd_2");
							String Eob_item_prdSrv_cdng_cd_3 = rseteob.getString("Eob_item_prdSrv_cdng_cd_3");
							String Eob_item_ext_val_cdng_code1 = rseteob.getString("Eob_item_ext_val_cdng_code1");
							String Eob_item_ext_val_cdng_code2 = rseteob.getString("Eob_item_ext_val_cdng_code2");
							String Eob_item_ext_val_cdng_code3 = rseteob.getString("Eob_item_ext_val_cdng_code3");
							String Eob_item_ext_val_cdng_code4 = rseteob.getString("Eob_item_ext_val_cdng_code4");
							String Eob_item_ext_val_cdng_code5 = rseteob.getString("Eob_item_ext_val_cdng_code5");
							String Eob_item_mod_cdng_cd_1 = rseteob.getString("Eob_item_mod_cdng_cd_1");
							String Eob_item_mod_cdng_cd_2 = rseteob.getString("Eob_item_mod_cdng_cd_2");
							String Eob_item_mod_cdng_cd_3 = rseteob.getString("Eob_item_mod_cdng_cd_3");
							String Eob_item_mod_cdng_cd_4 = rseteob.getString("Eob_item_mod_cdng_cd_4");
							String Eob_item_mod_cdng_cd_5 = rseteob.getString("Eob_item_mod_cdng_cd_5");

							String Eob_item_quant_val1 = rseteob.getString("Eob_item_quant_val");
							String eob_type_cdng_cd1 = rseteob.getString("eob_type_cdng_cd");
							String eob_type_cdng_cd2 = rseteob.getString("eob_type_cdng_cd2");
							String Eob_item_quant_val3 = rseteob.getString("Eob_item_quant_val3");
							String Eob_ct_qual_cdng_cd1 = rseteob.getString("Eob_ct_qual_cdng_cd");
							String Eob_cartm_prov_ident_val1 = rseteob.getString("Eob_cartm_prov_ident_val1");
							String Eob_cartm_prov_ident_val2 = rseteob.getString("Eob_cartm_prov_ident_val2");
							String Eob_cartm_prov_ident_val3 = rseteob.getString("Eob_cartm_prov_ident_val3");
							String eob_ct_prov_gern_id = rseteob.getString("eob_ct_prov_gern_id");
							String Eob_crtm_ext_valuecdng_cd = rseteob.getString("Eob_crtm_ext_valuecdng_cd");
							String Eob_billablePeriod_start = rseteob.getString("Eob_billablePeriod_start");
							String Eob_billablePeriod_end = rseteob.getString("Eob_billablePeriod_end");
							String clm_dgns_0_cd = rseteob.getString("clm_dgns_0_cd");
							String clm_dgns_1_cd = rseteob.getString("clm_dgns_1_cd");//Eob.diagnosis[N].diagnosisCodeableConcept.coding.code
							String clm_dgns_2_cd = rseteob.getString("clm_dgns_2_cd");
							String clm_dgns_3_cd = rseteob.getString("clm_dgns_3_cd");
							String clm_dgns_4_cd = rseteob.getString("clm_dgns_4_cd");
							String clm_dgns_5_cd = rseteob.getString("clm_dgns_5_cd");
							String Eob_disposition = rseteob.getString("Eob_disposition");
							String Eob_ident_val_clm_cntrl_num = rseteob.getString("Eob_ident_val");
							String Eob_ident_val_clm_cntrl_num_2 = rseteob.getString("Eob_ident_val_clm_cntrl_num_2");
							String eob_ident_val_presc_serv = rseteob.getString("eob_ident_val_presc_serv");
							String clm_prsbng_prvdr_gnrc_id_num = rseteob.getString("eob_fac_ident_val");
							String clm_phrmcy_srvc_type_cd = rseteob.getString("eob_fac_ext_valcdng_code");
							String Eob_sI_tim_Dt = rseteob.getString("Eob_sI_tim_Dt");
							String eob_si_val_q_val2 = rseteob.getString("eob_sivalq_val");
							String Eob_si_cdng_cd_cd = rseteob.getString("Eob_si_cdng_cd_cd");
							String eob_si_val_q_val = rseteob.getString("eob_si_val_q_val");

//							eobisd = rseteob.getString("Eob_item_servicedDate");
//							String eobipscc = rseteob.getString("Eob_item_productorService_coding_code");
//							--eob_item_adjudication_amnt_val_5
//									--eob_item_quant_val
//									--eob_type_cdng_cd
//									--eob_item_prsrv_cdng_cd_3


							itemc.setSequence(Integer.parseInt(eobitemseq));
							itemc2.setSequence(Integer.parseInt(eobitemseq2));
							Type eoblocCC = new org.hl7.fhir.r4.model.CodeableConcept();
							Type valcod = new org.hl7.fhir.r4.model.Coding();
							org.hl7.fhir.r4.model.Extension eoblocext= new org.hl7.fhir.r4.model.Extension();

							eoblocCC.setId("LocationCodeableConcept");
							valcod.setId("Value Coding Code");
							valcod.setIdBase(eobilocccextvalcd);//doubtfull must check

							eoblocext.setValue(valcod);
							eoblocCC.addExtension(eoblocext);
						//	Coding eobloccdng = new Coding();
							//eobloccdng.setCode(Eob_item_loc_cc_cdng);
						//	eoblocCC.setUserData("LocationCodeableConceptCoding",eobloccdng);//not displaying
							eoblocCC.setId(Eob_item_loc_cc_cdng);
							itemc.setLocation(eoblocCC);
							CodeableConcept itemcat= new CodeableConcept();
							Coding itencatcdng= new Coding();


							Type extvc= new Quantity();
							itencatcdng.setCode(Eob_item_cat_cdng_cd);
							itemcat.addCoding(itencatcdng);
							itemc.setCategory(itemcat);

							extvc.setId(Eob_item_ext_val_cdng_code1);
							eobext1.setValue(extvc);
							eobext1.setId("ValueCoding");
							//eobext1.setUserData("ValueCoding",extcdng1);//not dispalying

							extvc= new Quantity();
							extvc.setId(Eob_item_ext_val_cdng_code2);
							eobext2.setValue(extvc);
							eobext2.setId("ValueCoding");
							//eobext2.setUserData("ValueCoding",extcdng2); //not displaying

							extvc= new Quantity();
							extvc.setId(Eob_item_ext_val_cdng_code3);
							eobext3.setValue(extvc);
							eobext3.setId("ValueCoding");
						//	eobext3.setUserData("ValueCoding",extcdng3);//not displaying

							extvc= new Quantity();
							extvc.setId(Eob_item_ext_val_cdng_code4);
							eobext4.setValue(extvc);
							eobext4.setId("ValueCoding");
							//eobext4.setUserData("ValueCoding",extcdng4);//not displaying

							extvc= new Quantity();
							extvc.setId(Eob_item_ext_val_cdng_code5);
							eobext5.setValue(extvc);
							eobext5.setId("ValueCoding");
						//	eobext5.setUserData("ValueCoding",extcdng5);//not displaying

							Type serviced = new Period();
							Period itemperiod = new Period();
							Date serviceddatest = new SimpleDateFormat("yyyy-mm-dd").parse(Eob_item_servper_st);
							Date serviceddatend = new SimpleDateFormat("yyyy-mm-dd").parse(Eob_item_servper_end);
							itemperiod.setStart(serviceddatest);
							itemperiod.setEnd(serviceddatend);
							Base serperbase= new Period();
						//	serperbase.setUserData("Claim Line Serviced Period",itemperiod);
							//serviced.setProperty("servicedPeriod",serperbase);
							serviced.setId(itemperiod.toString());
							//serviced.setUserData("Claim Line Serviced Period",itemperiod);
							itemc.setServiced(serviced);
							cdng.setCode(Eob_item_prdSrv_cdng_cd);
							cdng.setDisplay("Refers to Claim Control Number: "+Eob_ident_val_clm_cntrl_num);
							List <Coding>  servcdngl = new ArrayList<>();
							boolean iscond = false;
							boolean truth = false;

							try{
								List<Coding> itemL= itemc.getProductOrService().getCoding();
								for(Coding cd : itemL)
								{
									String tcd= cd.getCode();
									if (Eob_item_prdSrv_cdng_cd!=null)
									{
										iscond = tcd.equals(Eob_item_prdSrv_cdng_cd);
									}
								}
								if(iscond==true)
								{
									truth=true;
								}}
							catch(Exception ex){
							}
							if(truth==false)
							{
								pserv.addCoding(cdng);
								itemc.setProductOrService(pserv);
							}

							cdng2.setCode(Eob_item_prdSrv_cdng_cd_2);
							cdng2.setDisplay("Refers to Claim Control Number 2: "+Eob_ident_val_clm_cntrl_num_2);
							try{
								List<Coding> itemL2= itemc2.getProductOrService().getCoding();
								for(Coding cd : itemL2)
								{
									String tcd= cd.getCode();
									if (Eob_item_prdSrv_cdng_cd_2!=null)
									{
										iscond = tcd.equals(Eob_item_prdSrv_cdng_cd_2);
									}
								}
								if(iscond==true)
								{
									truth=true;
								}}
							catch(Exception ex){
							}
							if(truth==false)
							{
								pserv2.addCoding(cdng2);
								itemc2.setProductOrService(pserv2);
							}

							cdng3.setCode(Eob_item_prdSrv_cdng_cd_3);
							cdng3.setDisplay("Refers to Claim Line Prescription Service Reference Number: "+eob_ident_val_presc_serv);

							try{
								List<Coding> item3L= itemc3.getProductOrService().getCoding();
								for(Coding cd : item3L)
								{
									String tcd= cd.getCode();
									if (Eob_item_prdSrv_cdng_cd_3!=null)
									{
										iscond = tcd.equals(Eob_item_prdSrv_cdng_cd_3);
									}
								}
								if(iscond==true)
								{
									truth=true;
								}}
							catch(Exception ex){
							}
							if(truth==false)
							{
								pserv3.addCoding(cdng3);
								itemc3.setProductOrService(pserv3);
							}

							Money adjudimoney= new Money();
							adjudimoney.setValue(Double.parseDouble(Eob_item_adjud_amnt_val_1));
							adjudiitem11.setAmount(adjudimoney);
							if(!acompl.contains(adjudiitem11))
							{acompl.add(adjudiitem11);}

							adjudimoney= new Money();
							adjudimoney.setValue(Double.parseDouble(Eob_item_adjud_amnt_val_2));
							adjudiitem12.setAmount(adjudimoney);
							if(!acompl.contains(adjudiitem12))
							{acompl.add(adjudiitem12);}

							adjudimoney= new Money();
							adjudimoney.setValue(Double.parseDouble(Eob_item_adjud_amnt_val_3));
							adjudiitem21.setAmount(adjudimoney);
							if(!acompl2.contains(adjudiitem21))
							{acompl2.add(adjudiitem21);}
							adjudimoney= new Money();
							adjudimoney.setValue(Double.parseDouble(Eob_item_adjud_amnt_val_4));
							adjudiitem22.setAmount(adjudimoney);
							if(!acompl2.contains(adjudiitem22))
							{acompl2.add(adjudiitem22);}

							adjudimoney= new Money();
							adjudimoney.setValue(Double.parseDouble(Eob_item_adjud_amnt_val_5));
							adjudiitem31.setAmount(adjudimoney);
							if(!acompl3.contains(adjudiitem31))
							{acompl3.add(adjudiitem31);}

							Quantity itemquan = new Quantity();
							Quantity itemquan3 = new Quantity();
							itemquan.setValue(Long.parseLong(Eob_item_quant_val1));
							itemc.setQuantity(itemquan);
							tpcdng.setDisplay("Claim Type Code");
							tpcdng.setCode(eob_type_cdng_cd1);
							tpcdng2.setDisplay("Claim Type Code");
							tpcdng2.setCode(eob_type_cdng_cd2);

							modcdng1.setCode(Eob_item_mod_cdng_cd_1);
							modcdng1.setDisplay("hcpcs_1_mdfr_cd");
							modcdng2.setCode(Eob_item_mod_cdng_cd_2);
							modcdng2.setDisplay("hcpcs_2_mdfr_cd");
							modcdng3.setCode(Eob_item_mod_cdng_cd_3);
							modcdng3.setDisplay("hcpcs_3_mdfr_cd");
							modcdng4.setCode(Eob_item_mod_cdng_cd_4);
							modcdng4.setDisplay("hcpcs_4_mdfr_cd");
							modcdng5.setCode(Eob_item_mod_cdng_cd_5);
							modcdng5.setDisplay("hcpcs_5_mdfr_cd");

							itencatcdng.setCode(eob_item_cat_cdng_cd_2);
							itemcat.addCoding(itencatcdng);
							itemc2.setCategory(itemcat);
							itemquan3.setValue(Double.parseDouble(Eob_item_quant_val3));
							itemc3.setQuantity(itemquan3);

							CareTeamComponent eobctc1= new CareTeamComponent();
							CareTeamComponent eobctc2= new CareTeamComponent();
							CareTeamComponent eobctc3= new CareTeamComponent();
							Reference eobprovref= new Reference();
							Reference eobprovref2= new Reference();
							Reference eobprovref3= new Reference();
							Identifier provident = new Identifier();
							CodeableConcept ctccc= new CodeableConcept();
							Coding ctcdng= new Coding();
							provident.setValue(Eob_cartm_prov_ident_val1);
							eobprovref.setIdentifier(provident);
							//eobprovref.setReference(Eob_cartm_prov_ident_val1);
							eobprovref.setDisplay("Rendering Provider NPI Number");
							Identifier tempident = new Identifier();
						//	Reference tempref= new Reference();
						//	CareTeamComponent tempctc= new CareTeamComponent();

							ctcdng.setCode(Eob_ct_qual_cdng_cd1);
							ctccc.addCoding(ctcdng);
							eobctc1.setQualification(ctccc);
							Extension eobext= new Extension();
						//	Coding ctcextcdng= new Coding();
						//	ctcextcdng.setCode(Eob_crtm_ext_valuecdng_cd);
							//eobext.setUserData("Rendering Provider Type Code",ctcextcdng);
							Type ctvc= new Coding();
							ctvc.setId(Eob_crtm_ext_valuecdng_cd);
							eobext.setValue(ctvc);
							eobctc1.addExtension(eobext);
							eobctc1.setProvider(eobprovref);

							boolean cond= false;
							boolean isnull=false;
							boolean istrue=false;

							try{
								for (CareTeamComponent ctct :ctcl){
									String tempLI = ctct.getProvider().getIdentifier().getValue();
									tempLI=tempLI.trim();
									if (Eob_cartm_prov_ident_val1!=null)
									{
										cond = tempLI.equals(Eob_cartm_prov_ident_val1.trim());
									}
									if (cond==true)
									{
										istrue=true;
									}
									else {
										isnull=true;
									}
								}
							}
							catch(Exception ex){

							}
							if (istrue == false )
							{
								ctcl.add(eobctc1);
							}

							provident= new Identifier();
							provident.setValue(Eob_cartm_prov_ident_val2);
							eobprovref2.setIdentifier(provident);
							//eobprovref.setReference(Eob_cartm_prov_ident_val1);
							eobprovref2.setDisplay("Pay-to Provider NPI Number");
							eobctc2 = new CareTeamComponent();
							eobctc2.setProvider(eobprovref2);

							try{
							//	for(int y=0;  y<ctcl.size()-1; y++){
								for (CareTeamComponent ctct :ctcl){
								String tempLI = ctct.getProvider().getIdentifier().getValue();
								tempLI=tempLI.trim();
								if (Eob_cartm_prov_ident_val2!=null)
								{
									cond = tempLI.equals(Eob_cartm_prov_ident_val2.trim());
								}
									if (cond==true)
									{
										istrue=true;
									}
							}
							}
							catch(Exception ex){
							//	ctcl.add(eobctc2);
							}
							if (istrue == false )
							{
								ctcl.add(eobctc2);
							}

							provident= new Identifier();
							provident.setValue(Eob_cartm_prov_ident_val3);
							eobprovref3.setIdentifier(provident);
							//eobprovref.setReference(Eob_cartm_prov_ident_val1);
							eobprovref3.setDisplay("Ordering Provider NPI Number");
							eobctc3 = new CareTeamComponent();
							eobctc3.setProvider(eobprovref3);

							try{
								for (CareTeamComponent ctct :ctcl){
									String tempLI = ctct.getProvider().getIdentifier().getValue();
									tempLI=tempLI.trim();
									if (Eob_cartm_prov_ident_val3!=null)
									{
										cond = tempLI.equals(Eob_cartm_prov_ident_val3.trim());
									}
									if (cond==true)
									{
										istrue=true;
									}
								}
							}
							catch(Exception ex){
							//	ctcl.add(eobctc3);
							}
							if (istrue == false )
							{
								ctcl.add(eobctc3);
							}
                            Reference claimrefer= new Reference();
							claimrefer.setDisplay(claimIdent);
							claimrefer.setReference("Claim/" + claimid.trim());
							providerreference2.setReference("Practitioner/" + pracid.trim());
							providerreference2.setDisplay(pname);
							patientReference2.setReference("Patient/" + patid.trim());
							patientReference2.setDisplay(patname);

                            eob.setClaim(claimrefer);
						//	eob.setFacility();

							eob.setPatient(patientReference2);
							eob.setProvider(providerreference2);

							eob.setCareTeam(ctcl);
							Period eobper= new Period();
							Date eobperst = new SimpleDateFormat("yyyy-mm-dd").parse(Eob_billablePeriod_start);
							Date eobperen = new SimpleDateFormat("yyyy-mm-dd").parse(Eob_billablePeriod_end);
							eobper.setStart(eobperst);
							eobper.setEnd(eobperen);
							eob.setBillablePeriod(eobper);
							DiagnosisComponent diagc = new DiagnosisComponent();
							Type diagtp= new CodeableConcept();
						//	Coding diagcdng= new Coding();
						//	diagcdng.setCode(clm_dgns_0_cd;
						//	diagcdng.setDisplay();
							diagtp.setId(clm_dgns_0_cd.trim());
							diagc = new DiagnosisComponent();
							diagc.setDiagnosis(diagtp);
							DiagnosisComponent tempdiagc= new DiagnosisComponent();
							Reference tempdiagref= new Reference();
							Type tdiagtp= new CodeableConcept();
							String tdiagcd= new String();

							try{for(int y=0;  y<diagl.size(); y++){
								tempident = new Identifier();
								tempdiagref= new Reference();
								tempdiagc= new DiagnosisComponent();
								tempdiagc	= diagl.get(y);
								tdiagtp = tempdiagc.getDiagnosis();
								tdiagcd = tdiagtp.getId();
								if (!diagtp.getId().equals(tdiagtp.getId()))
								{
									diagl.add(diagc);
								}
							}}
							catch(Exception ex){diagl.add(diagc);}

							diagtp= new CodeableConcept();
							diagtp.setId(clm_dgns_1_cd.trim());
							diagc = new DiagnosisComponent();
							diagc.setDiagnosis(diagtp); //Eob.diagnosis[N].diagnosisCodeableConcept.coding.code

							try{for(int y=0;  y<diagl.size(); y++){
								tempident = new Identifier();
								tempdiagref= new Reference();
								tempdiagc= new DiagnosisComponent();
								tempdiagc	= diagl.get(y);
								tdiagtp = tempdiagc.getDiagnosis();
								tdiagcd = tdiagtp.getId();
								if (!diagtp.getId().equals(tdiagtp.getId()))
								{
									diagl.add(diagc);
								}
							}}
							catch(Exception ex){diagl.add(diagc);}

							diagtp= new CodeableConcept();
							diagtp.setId(clm_dgns_2_cd.trim());
							diagc = new DiagnosisComponent();
							diagc.setDiagnosis(diagtp);

							try{for(int y=0;  y<diagl.size(); y++){
								tempident = new Identifier();
								tempdiagref= new Reference();
								tempdiagc= new DiagnosisComponent();
								tempdiagc	= diagl.get(y);
								tdiagtp = tempdiagc.getDiagnosis();
								tdiagcd = tdiagtp.getId();
								if (!diagtp.getId().equals(tdiagtp.getId()))
								{
									diagl.add(diagc);
								}
							}}
							catch(Exception ex){diagl.add(diagc);}

							diagtp= new CodeableConcept();
							diagtp.setId(clm_dgns_3_cd.trim());
							diagc = new DiagnosisComponent();
							diagc.setDiagnosis(diagtp);

							try{for(int y=0;  y<diagl.size(); y++){
								tempident = new Identifier();
								tempdiagref= new Reference();
								tempdiagc= new DiagnosisComponent();
								tempdiagc	= diagl.get(y);
								tdiagtp = tempdiagc.getDiagnosis();
								tdiagcd = tdiagtp.getId();
								if (!diagtp.getId().equals(tdiagtp.getId()))
								{
									diagl.add(diagc);
								}
							}}
							catch(Exception ex){diagl.add(diagc);}

							diagtp= new CodeableConcept();
							diagtp.setId(clm_dgns_4_cd.trim());
							diagc = new DiagnosisComponent();
							diagc.setDiagnosis(diagtp);

							try{for(int y=0;  y<diagl.size(); y++){
								tempident = new Identifier();
								tempdiagref= new Reference();
								tempdiagc= new DiagnosisComponent();
								tempdiagc	= diagl.get(y);
								tdiagtp = tempdiagc.getDiagnosis();
								tdiagcd = tdiagtp.getId();
								if (!diagtp.getId().equals(tdiagtp.getId()))
								{
									diagl.add(diagc);
								}
							}}
							catch(Exception ex){diagl.add(diagc);}

							diagtp= new CodeableConcept();
							diagtp.setId(clm_dgns_5_cd.trim());
							diagc = new DiagnosisComponent();
							diagc.setDiagnosis(diagtp);


							try{for(int y=0;  y<diagl.size(); y++){
								tempident = new Identifier();
								tempdiagref= new Reference();
								tempdiagc= new DiagnosisComponent();
								tempdiagc	= diagl.get(y);
								tdiagtp = tempdiagc.getDiagnosis();
								tdiagcd = tdiagtp.getId();
								if (!diagtp.getId().equals(tdiagtp.getId()))
								{
									diagl.add(diagc);
								}
							}}
							catch(Exception ex){diagl.add(diagc);}



							eob.setDisposition(Eob_disposition);
							Identifier eobident= new Identifier() ;

							eobident.setValue(Eob_ident_val_clm_cntrl_num.trim());
							try{
								for (Identifier eobILI :eobidentl){
									String tempLI = eobILI.getValue();
									tempLI=tempLI.trim();
									if (Eob_ident_val_clm_cntrl_num!=null)
									{
										cond = tempLI.equals(Eob_ident_val_clm_cntrl_num.trim());
									}
									if (cond==true)
									{
										istrue=true;
									}

								}
							}
							catch(Exception ex){

							}
							if (istrue == false )
							{
							//	eobident.setUse(Identifier.IdentifierUse.OFFICIAL);
								eobidentl.add(eobident);
							}

							eobident.setValue(Eob_ident_val_clm_cntrl_num_2);
							try{
								for (Identifier eobILI :eobidentl){
									String tempLI = eobILI.getValue();
									tempLI=tempLI.trim();
									if (Eob_ident_val_clm_cntrl_num_2!=null)
									{
										cond = tempLI.equals(Eob_ident_val_clm_cntrl_num_2.trim());
									}
									if (cond==true)
									{
										istrue=true;
									}

								}
							}
							catch(Exception ex){

							}
							if (istrue == false )
							{
								eobidentl.add(eobident);
							}

							eobident.setValue(eob_ident_val_presc_serv);
							try{
								for (Identifier eobILI :eobidentl){
									String tempLI = eobILI.getValue();
									tempLI=tempLI.trim();
									if (eob_ident_val_presc_serv!=null)
									{
										cond = tempLI.equals(eob_ident_val_presc_serv.trim());
									}
									if (cond==true)
									{
										istrue=true;
									}

								}
							}
							catch(Exception ex){

							}
							if (istrue == false )
							{
								eobidentl.add(eobident);
							}
							Reference facref= new Reference();
							Identifier eobfacident = new Identifier();

									 Extension facext= new Extension();
						//	 Coding extcdng= new Coding();
							 Type etab= new CodeableConcept();
							//extcdng.setCode(clm_phrmcy_srvc_type_cd);
                            etab.setId(clm_phrmcy_srvc_type_cd);
							eobfacident.setValue(clm_prsbng_prvdr_gnrc_id_num);
							facref.setIdentifier(eobfacident);
							facref.setDisplay("Claim Prescribing Provider Generic ID Number");
							//facext.setUserData(,);
							facext.setId("Claim Pharmacy Service Type Code");
							facext.setValue(etab);
							facref.addExtension(facext);
							eob.setFacility(facref);
							SupportingInformationComponent sic= new SupportingInformationComponent();
							SupportingInformationComponent sic2= new SupportingInformationComponent();
							Date sidt = new SimpleDateFormat("yyyy-mm-dd").parse(Eob_sI_tim_Dt);
						//	Type sitime= new Timing();
						//	sitime.setId(Eob_sI_tim_Dt);
						//	sic.setTiming(sitime);
							CodeableConcept sicon= new CodeableConcept();
							Coding sicod= new Coding();
							sicod.setCode(Eob_si_cdng_cd_cd);
							sicod.setDisplay("Claim Dispense as Written (DAW) Product Selection Code");
							CodeSystem daw = new CodeSystem();
							//hasdaw = sicon.hasCoding("Claim Dispense as Written (DAW) Product Selection Code",Eob_si_cdng_cd_cd);
							sicon.addCoding(sicod);
							sic.setCode(sicon);
							Type sicvq= new Quantity();
							Quantity valq= new Quantity();
							Quantity valq2= new Quantity();
							valq.setValue(Double.parseDouble(eob_si_val_q_val));
							sicvq.setUserData("Claim Line Days’ Supply Quantity",valq);
							sic.setValue(sicvq);

							try{for(int y=0;  y<sicl.size(); y++){
								List<Coding> tcdng = new ArrayList<>();
								tcdng =sicl.get(y).getCode().getCoding();
								for(int z=0;  z<tcdng.size(); z++){
									Coding tmcdng = new Coding();
									tmcdng = tcdng.get(z);
									if (!sicod.getCode().equals(tmcdng.getCode()))
									{
										sicl.add(sic) ;
									}
								}
							}}
							catch(Exception ex)
							{
								sicl.add(sic) ;
							}

							sicvq= new Quantity();
							valq2.setValue(Double.parseDouble(eob_si_val_q_val2));
							sicvq.setUserData("Claim Line Days’ Supply Quantity",valq2);
							sic2.setValue(sicvq);
							try{for(int y=0;  y<sicl.size(); y++){
								List<Coding> tcdng = new ArrayList<>();
								tcdng =sicl.get(y).getCode().getCoding();
								for(int z=0;  z<tcdng.size(); z++){
									Coding tmcdng = new Coding();
									tmcdng = tcdng.get(z);
									if (!sicod.getCode().equals(tmcdng.getCode()))
									{
										sicl.add(sic2) ;
									}
								}
							}}
							catch(Exception ex)
							{
								sicl.add(sic2) ;
							}



//							eob.setSupportingInfo();// 	 Eob_sI_tim_Dt, done
//							eob.setSupportingInfo()	//	eob_si_timedt_2, duplicate
//							eob.setSupportingInfo()	//	 Eob_si_tim_dt, duplicate
//								eob.setSupportingInfo()//	 eob_sivalq_val, done
//							eob.setSupportingInfo()	//	  Eob_si_cdng_cd_cd, done
//							eob.setSupportingInfo()	// eob_si_val_q_val, done
//							eob.setIdentifier()//Eob_ident_val_clm_cntrl_num , done
//							eob.setIdentifier()	//	 Eob_ident_val_clm_cntrl_num_2, done
//							eob.setFacility()	// clm_prsbng_prvdr_gnrc_id_num	eob_fac_ident_val, done
//							eob.setStatus()	//	eob_status_3, done
//							eob.setIdentifier()	//	eob_ident_val_presc_serv done
//								eob.setFacility()//		eob_fac_ext_valcdng_code done

//							eob.setCareTeam()//Eob_ct_qual_cdng_cd   done
//							eob.setCareTeam()	//	eob_ct_prov_ident_val, done
//							eob.setCareTeam()	//	eob_ct_prov_identval, done
//							eob.setCareTeam()	///	eob_ct_prov,
//							eob.setCareTeam();//Eob_cartm_prov_ident_val done
//							eob.setCareTeam()//ext valuecdng_cd        done
//							eob.setItem();//Eob_item_loc_cc_ext_val_cd done
//							eob.setItem();//Eob_item_cat_cdng_cd      done
//							eob.setItem();//Eob_item_loc_cc_cdng,     done
//							eob.setItem();//Eob_item_servper_st,      done
//							eob.setItem()//Eob_item_servper_end,      done
//							eob.setItem()//Eob_item_prdSrv_cdng_cd,    done
//							eob.setItem()//	Eob_item_adjud_amnt_val_1, done
//							eob.setItem()//	 Eob_item_ext_val_cd,      done
//							eob.setItem();//Eob_item_ext_val_cdng_code, done
//							eob.setItem();// Eob_item_adjud_amnt_val_2, done
//							eob.setItem()//Eob_item_quant_val1,        done
//							eob.setItem();// Eob_item_mod_cdng_cd_1    done
//							eob.setItem()	//Eob_item_mod_cdng_cd_2 ,  done
//							eob.setItem()// Eob_item_mod_cdng_cd_3,     done
//							eob.setItem();//Eob_item_mod_cdng_cd_4,    done
//							eob.setItem()// Eob_item_mod_cdng_cd_5,    done
//							eob.setItem()	//Eob_item_ext_valcdng_cd,  done
//							eob.setItem()	//	eob_item_seq_1,        done
//							eob.setItem()	//	 eob_item_cat_cdng_cd_2, done
//							eob.setItem()	//	Eob_item_location_cc_cdng, done
//							eob.setItem()	//	eob_item_servper_st,     done
//							eob.setItem()	////	Eob_item_servper_en, done
//							eob.setItem()//	 Eob_item_prodserv_cdng_cd,  done
//							eob.setItem()	//	 eob_item_adjud_amnt_val_3,done
//							eob.setItem()	//	eob_item_ext_valcdng_cd_2 ,done
//							eob.setItem()	//	 eob_item_exten_val_cdng_cd_3,done
//							eob.setItem()	//	eob_item_exten_val_cdng_cd_2,done
//							eob.setItem()	//	 Eob_item_adjud_amnt_val_4, done
//							eob.setItem()	//		eob_item_prodsrv_cdng_cd,done
//							eob.setItem()	//	Eob_item_servper_st,
//							eob.setItem()	//	Eob_item_quant_val2 ,done
//							eob.setItem()//	 eob_item_adjud_amnt_val_5, done
//
//							eob.setType()	//	 eob_type_cdng_cd, done
//							eob.setType()		//eob_type_cdng_cd,done
//							eob.setStatus()	//	eob_status_2, done
//							eob.setType();//eob_tp_cdng_code done
//							eob.setBillablePeriod();//Eob_billablePeriod_start Eob_billablePeriod_end done
//							eob.setDiagnosis()//	Eob_diag_cdng_cd, clm_dgns_0_cd done
//							eob.setExtension();//Eob_ext_val_cdng_code, ?
//							eob.setStatus();// 	 Eob_status,  done
//							eob.setDisposition()// Eob_disposition, done
//									eob.setDiagnosis() //Eob_dgns_dgns_cc_cdng_1,clm_dgns_1_cd done
//							eob.setDiagnosis()	// 	Eob_dgns_dgns_cc_cdng_1_2_cd,clm_dgns_2_cd done
//							eob.setDiagnosis()	// 	Eob_dgns_dgns_cc_cdng_1_3_cd,clm_dgns_3_cd done
//							eob.setDiagnosis()	// 	Eob_dgns_dgns_cc_cdng_1_4_cd,clm_dgns_4_cd done
//							eob.setDiagnosis()	// 	Eob_dgns_dgns_cc_cdng_1_5_cd,clm_dgns_5_cd done
//							eob.setDiagnosis()//			Eob_dgns_dgns_cc_cdng_1_6_cd,clm_dgns_6_cd
//									eob.setDiagnosis()		//		Eob_dgns_dgns_cc_cdng_1_7_cdclm_dgns_7cd
//							eob.setBillablePeriod()	//	eob_billper_st//	eob_billper_end, duplicate
//							eob.setDisposition()	//	Eob_disposition_2, duplicate



							eob.setIdentifier(eobidentl);

							String eobStatus = rseteob.getString("eob_status").trim();
							if (eobStatus.equals("0")) {
								eob.setStatus(ExplanationOfBenefitStatus.ACTIVE);
							} else if (eobStatus.equals("1")) {
								eob.setStatus(ExplanationOfBenefitStatus.CANCELLED);
							} else if (eobStatus.equals("2")) {
								eob.setStatus(ExplanationOfBenefitStatus.DRAFT);
							}
						} catch (Exception ex) {
							ex.printStackTrace();
							Date date = new Date();
							String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());

							sql = "insert into batch_log values ('" + timeStamp + "','eob','" + ex.toString() + "','" + cliniccount + "','" + praccount + "','" + patcount + "','" + claimcount + "','" + eobcount + "',false)";
							System.out.println("The EOB error SQL is: " + sql + "");
							Connection conntop18 = DriverManager.getConnection("jdbc:postgresql://3.000.000.000:5432/cclfdb", "postgres", "password");
							Statement stmt8 = conntop18.createStatement();
							stmt8.executeQuery(sql);
						}

					} //end of eob while loop and incrementer



//					prccoding.setCode(c3clm_prcdr_cd);
//					prccoding.setDisplay("Procedure Code");
//					eobprccd.addCoding(prccoding);
//
//					if (eobprctpL.contains(eobprccd))
//					{
//					}
//					else {
//						eobprctpL.add(eobprccd);
//					}
//					eobprcc.setType(eobprctpL);
//					if (pcL.contains(eobprcc))
//					{
//					}
//					else {
//						pcL.add(eobprcc);
//					}
//
//					eob.setProcedure(pcL);
//
//					if(eobcodingtpL.contains(coding))
//					{}
//					else
//					{
//						eobcodingtpL.add(coding);
//					}
//					prccd.setCoding(eobcodingtpL);//c4prod_type Eob.diagnosis[N].type.coding.code
//					if(diagtpL.contains(prccd))
//					{}
//					else
//					{
//						diagtpL.add(prccd);
//					}
//					diagnosis.setType(diagtpL);//c4prod_type Eob.diagnosis[N].type.coding.code
//					ldc.add(prdiagnosis);
//					ldc.add(diagnosis);
//					ldc.add(addiagnosis);
//					eob.addIdentifier().setValue(claimIdent);
//					eob.addSupportingInfo(sic);
//					eob.addInsurance(insurC);
//					eob.addTotal(totAmtCmp);
//					eob.setType(etcco); // CLM_TYPE_CD from cclf1

					eob.setDiagnosis(diagl);
					eobextl.add(eobext1);
					eobextl.add(eobext2);
					eobextl.add(eobext3);
					eobextl.add(eobext4);
					eobextl.add(eobext5);
					modcc1.addCoding(modcdng1);
					modcc2.addCoding(modcdng2);
					modcc3.addCoding(modcdng3);
					modcc4.addCoding(modcdng4);
					modcc5.addCoding(modcdng5);
					modccl.add(modcc1);
					modccl.add(modcc2);
					modccl.add(modcc3);
					modccl.add(modcc4);
					modccl.add(modcc5);
					itemc.setModifier(modccl);
					itemc.setExtension(eobextl);
					theiteml.add(itemc);
					theiteml.add(itemc2);
					theiteml.add(itemc3);
					eob.setItem(theiteml);
					eob.setSupportingInfo(sicl);
					itemc.setAdjudication(acompl);
					itemc2.setAdjudication(acompl2);
					itemc.setAdjudication(acompl3);

					if (tpcdngl.contains(tpcdng))
					{	tpcdngl.add(tpcdng);}
					if (tpcdngl.contains(tpcdng2))
					{	tpcdngl.add(tpcdng2);}
					eobtypecc.setCoding(tpcdngl);
					eob.setType(eobtypecc);
					//	FhirApiReq geteobidCall = new FhirApiReq();
					boolean testeobident =false;
					// testeobident = geteobidCall.ApiGet(claimIdent, "ExplanationOfBenefit");
					if (testeobident == false) {


						boolean isempty=eob.isEmpty();
						if (!isempty) {
							eob.setMeta(clmeta);
							serOrg = jsonparser.encodeResourceToString(eob);
							System.out.println(serOrg);
							System.out.println("Everything ok see eob json above");

							//		geteobidCall.ApiPost(serOrg, "ExplanationOfBenefit");
						}
						if (isempty)
						{
							System.out.println("Eob empty");
						}

					}

															//end of while nested eob loop
														} catch (Exception ex) {
															ex.printStackTrace();
															Date date = new Date();
															String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
															sql = "insert into batch_log values ('" + timeStamp + "','eob','" + ex.toString() + "','" + cliniccount + "','" + praccount + "','" + patcount + "','" + claimcount + "','" + eobcount + "',false)";
															System.out.println("The EOB error SQL is: " + sql + "");
															Connection conntop18 = DriverManager.getConnection("jdbc:postgresql://3.236.102.222:5432/cclfdb", "postgres", "Dataqhealth1");
															Statement stmt8 = conntop18.createStatement();
															stmt8.executeQuery(sql);
														}
													}//end of if condition for calim if it alrady exists
												}//end of while nested claim loop
											} catch (Exception ex) {
												ex.printStackTrace();
												Date date = new Date();
												String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
												sql = "insert into batch_log values ('" + timeStamp + "','Claim','" + ex.toString() + "','" + cliniccount + "','" + praccount + "','" + patcount + "','" + claimcount + "','" + eobcount + "',false)";
												System.out.println("The claim error SQL is: " + sql + "");
												Connection conntop5 = DriverManager.getConnection("jdbc:postgresql://3.000.000.000:5432/cclfdb", "postgres", "password");
												Statement stmt9 = conntop5.createStatement();
												stmt9.executeQuery(sql);
											}//end of try cathc for nested clain

										}//end of pat resultset loop
									} catch (Exception ex) {
										ex.printStackTrace();
										Date date = new Date();
										String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());

										sql = "insert into batch_log values ('" + timeStamp + "','Patient','" + ex.toString() + "','" + cliniccount + "','" + praccount + "','" + patcount + "','" + claimcount + "','" + eobcount + "',false)";
										System.out.println("The Patient error SQL is: " + sql + "");
										Connection conntop4 = DriverManager.getConnection("jdbc:postgresql://3.000.000.000:5432/cclfdb", "postgres", "password");
										Statement stmt10 = conntop4.createStatement();
										stmt10.executeQuery(sql);
									}


								}//end of prac resultset loop
							} catch (Exception ex) {
								ex.printStackTrace();
								Date date = new Date();
								String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
								sql = "insert into batch_log values ('" + timeStamp + "','Practitioner','" + ex.toString() + "','" + cliniccount + "','" + praccount + "','" + patcount + "','" + claimcount + "','" + eobcount + "',false)";
								System.out.println("The Practitioner error SQL is: " + sql + "");
								Connection conntop3 = DriverManager.getConnection("jdbc:postgresql://3.000.000.000:5432/cclfdb", "postgres", "password");
								Statement stmt11 = conntop3.createStatement();
								stmt11.executeQuery(sql);
							}//	}//end of practitioner loop

//						}//end of clinic resultset loop
//
//						} catch (Exception ex) {
//							ex.printStackTrace();
//							Date date = new Date();
//							String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
//							sql = "insert into batch_log values ('"+timeStamp+"','Clinic','"+ex.toString()+"','"+cliniccount+"','"+praccount+"','"+patcount+"','"+claimcount+"','"+eobcount+"',false)";
//							System.out.println("The clinic error SQL is: " + sql + "");
//							Connection conntop3 = DriverManager.getConnection("jdbc:postgresql://3.000.000.000:5432/cclfdb","postgres", "password");
//							Statement stmt12 = conntop3.createStatement();
//							stmt12.executeQuery(sql);
//						}
				//	}//end of clinic loop


				}//end of organization resultset while loop
				//rowCount=i;

		//	}//end of acolist
			// start of nested loop structure for practioner and patient

			System.out.println("Total number of records = " + rowCount);
			Date date = new Date();
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
			String sql43 = "insert into batch_log values ('"+timeStamp+"','Organization','--','"+cliniccount+"','"+praccount+"','"+patcount+"','"+claimcount+"','"+eobcount+"',true)";
			System.out.println("The final count SQL is: " + sql43 + "");
			Connection conntop2 = DriverManager.getConnection(
					"jdbc:postgresql://3.000.000.000:5432/cclfdb",
					"postgres", "password");
			Statement stmt13 = conntop2.createStatement();
			stmt13.executeQuery(sql43);
		}
		catch(SQLException | ClassNotFoundException ex) {
			ex.printStackTrace();
			Date date = new Date();
			String sql46 = "insert into batch_log values ('"+date.getTime()+"','Top Catch','"+ex.getMessage()+"','"+cliniccount+"','"+praccount+"','"+patcount+"','"+claimcount+"','"+eobcount+"',false)";
			System.out.println("The top error catch  SQL is: " + sql46 + "");
			try{Class.forName("org.postgresql.Driver");
				Connection conntop = DriverManager.getConnection(
						"jdbc:postgresql://3.000.000.000:5432/cclfdb",
						"postgres", "password");
				Statement stmt14 = conntop.createStatement();
				stmt14.executeQuery(sql46);
			}
			catch(Exception e2){e2.printStackTrace();}

		}  // Step


	}

	class DeceasedType extends Type {

		@Override
		protected Type typedCopy() {
			return null;
		}
		//public Enumerations isDeceased{}
	}
	public String ApiPost(String serOrg,String ResourceType )
	{
		String result ="1";
		try{
			HttpClient httpClient    = HttpClientBuilder.create().build();
			HttpPost postreq = new HttpPost("http://fhir.dataqhealth.com/fhir/"+ResourceType);
			StringEntity postingString = new StringEntity(serOrg, ContentType.APPLICATION_JSON);
			postreq.setEntity(postingString);
			HttpResponse response = httpClient.execute(postreq);
			InputStream is = response.getEntity().getContent();
			Reader reader = new InputStreamReader(is);
			BufferedReader bufferedReader = new BufferedReader(reader);
			StringBuilder builder = new StringBuilder();
			while (true) {
				try {
					String line = bufferedReader.readLine();
					if (line != null) {
						builder.append(line);
					} else {
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			String tempbuilder= builder.toString();
			System.out.println("success after api request: "+tempbuilder);
			JSONObject myResponse =  new JSONObject(tempbuilder.substring(tempbuilder.indexOf("{"), tempbuilder.lastIndexOf("}") + 1));
			result=myResponse.getString("id");
		}
		catch (ClientProtocolException | JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}


}

