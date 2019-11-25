package com.example.reservationApi.admin;

import org.springframework.stereotype.Service;

@Service
public class AdminService {
    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }


    public Admin save(Admin admin) {
        return adminRepository.save(admin);
    }
}
