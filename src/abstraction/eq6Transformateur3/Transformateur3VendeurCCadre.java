package abstraction.eq6Transformateur3;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import abstraction.eqXRomu.bourseCacao.BourseCacao;
import abstraction.eqXRomu.contratsCadres.Echeancier;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.contratsCadres.ExempleTransformateurContratCadre;
import abstraction.eqXRomu.contratsCadres.IAcheteurContratCadre;
import abstraction.eqXRomu.contratsCadres.IVendeurContratCadre;
import abstraction.eqXRomu.contratsCadres.SuperviseurVentesContratCadre;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.IProduit;

public class Transformateur3VendeurCCadre extends Transformateur3AcheteurCCadre implements IVendeurContratCadre{

	public Transformateur3VendeurCCadre() {
		super();
	}
	
	
	/**
	 * Thomas 
	 */
	public void next() {
		super.next();
		this.journalCC6.ajouter("=== Partie Vente chocolat ====================");
		for (Chocolat c : stockChoco.keySet()) { 
			if (stockChoco.get(c)-restantDu(c)>200) { 
				this.journalCC6.ajouter("   "+c+" suffisamment en stock pour passer un CC");
				double parStep = Math.max(100, (stockChoco.get(c)-restantDu(c))/24); // au moins 100, et pas plus que la moitie de nos possibilites divisees par 2
				Echeancier e = new Echeancier(Filiere.LA_FILIERE.getEtape()+1, 12, parStep);
				List<IAcheteurContratCadre> acheteurs = supCC.getAcheteurs(c);
				if (acheteurs.size()>0) {
					IAcheteurContratCadre acheteur = acheteurs.get(Filiere.random.nextInt(acheteurs.size()));
					journalCC6.ajouter("   "+acheteur.getNom()+" retenu comme acheteur parmi "+acheteurs.size()+" acheteurs potentiels");
					ExemplaireContratCadre contrat = supCC.demandeVendeur(acheteur, this, c, e, cryptogramme, false);
					if (contrat==null) {
						journalCC6.ajouter(Color.RED, Color.BLACK,"   echec des negociations");
					}
					else {
						this.contratsEnCours.add(contrat);
						journalCC6.ajouter(Color.GREEN, Color.BLACK, "   contrat signe");
					}
				} else {
					journalCC6.ajouter("   pas d'acheteur");
				}
			}
		}
		//On archive les contrats termines
		for (ExemplaireContratCadre c : this.contratsEnCours) {
			if (c.getQuantiteRestantALivrer()==0.0) {
				this.contratsTermines.add(c);
			}
		}
		for (ExemplaireContratCadre c : this.contratsTermines) {
			this.contratsEnCours.remove(c);
		}
		this.journalCC6.ajouter("=== Partie réception Fèves ====================");
	}
	/**
	 * Thomas
	 */
	public double restantDu(Chocolat c) {
		double res=0;
		for (ExemplaireContratCadre p : this.contratsEnCours) {
			if (p.getProduit().equals(c)) {
				res+=p.getQuantiteRestantALivrer();
			}
		}
		return res;
	}
	/**
	 * Thomas
	 */
	public List<Journal> getJournaux() {
		List<Journal> jx=super.getJournaux();
		return jx;
	}

	/**
	 * Thomas
	 */
	public boolean vend(IProduit produit) {
		return produit.getType().equals("Chocolat") && stockChoco.get((Chocolat)produit) -restantDu((Chocolat)produit)>200;
	}
	/**
	 * Thomas
	 */
	public Echeancier contrePropositionDuVendeur(ExemplaireContratCadre contrat) {
		return contrat.getEcheancier();
	}

	/**
	 * Thomas
	 */
	public double propositionPrix(ExemplaireContratCadre contrat) {
		double moyenne  = 0; 
		int p = 0;
		double prix = 0;
		for (ExemplaireContratCadre contratCC : contratsEnCours) {
			if (((Feve) contrat.getProduit()).equals((Feve) contratCC.getProduit())) {
				moyenne += contratCC.getPrix();
				p += 1;
			
			}
		}
		moyenne = moyenne/p;
		prix = moyenne + 0.5 * 1200 + 8;
		return 1.05 * prix;
	}
	/**
	 * Thomas
	 */
	public double contrePropositionPrixVendeur(ExemplaireContratCadre contrat) {
		return 0.95*contrat.getPrix();
	}

	/**
	 * Thomas
	 */
	public void notificationNouveauContratCadre(ExemplaireContratCadre contrat) {
		if(contrat.getAcheteur()==this) {
			journalCC6.ajouter(Color.ORANGE, Color.WHITE,"Nouveau contrat initié par un producteur");
			journalCC6.ajouter("Nouveau contrat accepté : "+"#"+contrat.getNumero()+" | Acheteur : "+contrat.getAcheteur()+" | Vendeur : "+contrat.getVendeur()+" | Produit : "+contrat.getProduit()+" | Quantité totale : "+contrat.getQuantiteTotale()+" | Prix : "+contrat.getPrix());	
			this.contratsEnCours.add(contrat);
		}
		else {
			System.out.println(">>>>>>>>>>>>>>>> nouveau contrat"+contrat);
			this.journalCC6.ajouter("Nouveau contrat de ventes " + contrat.getNumero());
			this.contratsEnCours.add(contrat);
		}
	}
	/**
	 * Thomas
	 */
	public double livrer(IProduit produit, double quantite, ExemplaireContratCadre contrat) {
		journalCC6.ajouter("Livraison de : "+quantite+", tonnes de :"+produit.getType()+" provenant du contrat : "+contrat.getNumero());
		stockChoco.put((Chocolat)produit, stockChoco.get((Chocolat)produit)-quantite);
		totalStocksChoco.retirer(this, quantite, cryptogramme);
		return quantite;
	}
}

