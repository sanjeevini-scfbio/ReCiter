package reciter.service.mongo.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reciter.database.mongo.model.InstitutionAfid;
import reciter.database.mongo.repository.InstitutionAfidRepository;
import reciter.service.InstitutionAfidService;

@Service("institutionAfidService")
public class InstitutionAfidServiceImpl implements InstitutionAfidService {

	@Autowired
	private InstitutionAfidRepository institutionAfidRepository;
	
	@Override
	public List<String> getAfidByInstitution(String institution) {
		System.out.println("institution: " + institution);
		List<InstitutionAfid> institutionAfids = institutionAfidRepository.findByInstitution(institution);
		List<String> afids = new ArrayList<>(institutionAfids.size());
		for (InstitutionAfid institutionAfid : institutionAfids) {
			afids.add(institutionAfid.getAfid());
		}
		return afids;
	}

	@Override
	public List<InstitutionAfid> findAll() {
		return institutionAfidRepository.findAll();
	}
}
