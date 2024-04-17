package abstraction.eq5Transformateur2;

import java.util.List;

import abstraction.eqXRomu.filiere.Banque;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.Gamme;

/** 
 * Cette classe a pour objectif de :
 *   - Déterminer le nombre de tonne à transformer
 *   - Calculer le coût de transformation
 *   - déterminer le besoin d'embauche / de licenciement
 *   - Calculer le coût de la masse Salariale
 */
public class Transformateur2MasseSalariale extends Transformateur2Acteur {
	protected int NbSalaries;
	protected double salaire;//salaire par step
	protected double coutLicenciement1Salarie;
	protected double capaciteTransformation;// tonnes transformées par 1 salarié par step
	protected Journal JournalMasseSalariale;
	protected double coutAdjuvants;//cout des adjuvants pour 1 tonne de chocolat
	protected double coutMachines;//cout des machines pour 1 tonne de chocolat
	
	////////////////////////////////////////////
	// Constructor & Initialization of values //
	////////////////////////////////////////////
	public Transformateur2MasseSalariale() {
		super();
		this.JournalMasseSalariale=new Journal(this.getNom()+" journal MS", this);
	}
	public void initialiser() {
		super.initialiser();
		NbSalaries = 100;
		salaire = 2000;
		coutLicenciement1Salarie = 4*salaire;
		capaciteTransformation = 3.7;
		coutAdjuvants = 1200;
		coutMachines = 8;
		
		this.JournalMasseSalariale.ajouter("_____________Initialement_______________________________________");
		this.JournalMasseSalariale.ajouter("Nombre de salarié :"+NbSalaries);
		this.JournalMasseSalariale.ajouter("coût d'un salarié par step :"+salaire);
		this.JournalMasseSalariale.ajouter("coût de licenciement d'un salarié :"+coutLicenciement1Salarie);
		this.JournalMasseSalariale.ajouter("coût entretien/achat des machines par step :"+coutMachines);
		this.JournalMasseSalariale.ajouter("coût 1 tonne d'Adjuvants :"+coutAdjuvants);
		this.JournalMasseSalariale.ajouter("1 salarié peut transformé "+capaciteTransformation+" tonnes de fèves en chocolat par step");
		this.JournalMasseSalariale.ajouter("________________________________________________________________");
	}
	

	////////////////////////////////////////////
	//             Transformation             //
	////////////////////////////////////////////
	public double TonnesTransformees(Feve f) {
		double tMaxTransformees = Math.min(this.getQuantiteEnStock(f, cryptogramme),this.NbSalaries*this.capaciteTransformation); //Quantite maximale a transformer
		double tonnesTransformees =0.9*tMaxTransformees; //On transforme 90% (peut etre modifie) de ce qu'on peut transformer au maximum
		Chocolat c = Chocolat.get(f.getGamme(), f.isBio(), f.isEquitable());
		//ChocolatDeMarque cm = new ChocolatDeMarque(c,"CacaoFusion",40);//Pourcentage de cacao a modifier
		//this.stockChocoMarque.put(cm, this.getQuantiteEnStock(cm,cryptogramme)+tonnesTransformees); //Modifie le stock de chocolat de marque
		this.stockFeves.put(f, this.getQuantiteEnStock(f,cryptogramme)-tonnesTransformees); //Modifie le stock de feves
		return tonnesTransformees; 
	}
	public double TotauxTonnesTransformees() {
		double totaux = 0;
		for (Feve f : Feve.values()) {
			totaux += this.TonnesTransformees(f);
		}
		this.JournalMasseSalariale.ajouter("On a Transformé au total "+totaux+" tonnes de chocolat");
		return totaux;
	}
	
	////////////////////////////////////////////
	//      Calcul Coût de transformation     //
	////////////////////////////////////////////
	public double CoutTransformation(ChocolatDeMarque cm, double tonnes) {
		return tonnes*coutMachines + tonnes*(100-cm.getPourcentageCacao())*coutAdjuvants ;
	}
	public double CoutTransformationTotal() {
		double coutTotal = 0;
		for (ChocolatDeMarque cm : chocosProduits) {
			if (cm.getGamme()!= Gamme.HQ) {
				double t = VariationStockChocoMarque.get(cm);
				coutTotal += this.CoutTransformation(cm,t);	
			}
		}
		this.JournalMasseSalariale.ajouter("Le cout total de la transformation est de"+coutTotal);
		return coutTotal;
	}
	
	////////////////////////////////////////////
	//         Calcul Masse Salariale         //
	////////////////////////////////////////////
	/* Embauche si le nbr de salarié n'est pas assez important
	 * Pas de licenciement pour l'instant
	 */
	public int EmbaucheLicenciement(double TonnesTransformees) {
		double CapaciteTransfoTotale = NbSalaries * capaciteTransformation;

		if (TonnesTransformees >= CapaciteTransfoTotale) {
			int embauche = (int) ((TonnesTransformees - CapaciteTransfoTotale)/capaciteTransformation);
			NbSalaries += embauche;
			this.JournalMasseSalariale.ajouter("On embauche"+embauche+"personnes");
			return embauche;
			
		}
		else {
			this.JournalMasseSalariale.ajouter("On ne licencie pas");
			return 0;
		}
	}
	public double CoutMasseSalariale(double TonnesTransformees) {
		double cout_salaire = NbSalaries * salaire;
		double cout_licenciement = 0;
		if (this.EmbaucheLicenciement(TonnesTransformees)<0) {
			cout_licenciement = this.EmbaucheLicenciement(TonnesTransformees) * coutLicenciement1Salarie ;
		}
		this.JournalMasseSalariale.ajouter("La masse salariale est de"+cout_salaire);
		return  cout_salaire + cout_licenciement;
	}
	
	////////////////////////////////////////////
	//        Next : paiments des coûts       //
	////////////////////////////////////////////
	public void next() {
		super.next();
		this.JournalMasseSalariale.ajouter("=== STEP "+Filiere.LA_FILIERE.getEtape()+" ====================");
		
		// Paiement des coût de la masse salariale
		double TotauxTransformees = this.TotauxTonnesTransformees();
		if (TotauxTransformees > 0.0) {
			Filiere.LA_FILIERE.getBanque().payerCout(Filiere.LA_FILIERE.getActeur(getNom()), this.cryptogramme, "Coût MS", this.CoutMasseSalariale(TotauxTransformees));
		}
		// Paiement des coût de transformation
		double TotalCout = this.CoutTransformationTotal();
		if (TotalCout > 0.0) {
			Filiere.LA_FILIERE.getBanque().payerCout(Filiere.LA_FILIERE.getActeur(getNom()), this.cryptogramme, "Coût Transformation" , TotalCout);
		}
	}

	/////////////////////////////////////
	//   Ajout du journal aux autres   //
	/////////////////////////////////////
	public List<Journal> getJournaux() {
		List<Journal> jx=super.getJournaux();
		jx.add(JournalMasseSalariale);
		return jx;
	}
}



