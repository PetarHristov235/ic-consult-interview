/*
 * AccessProtectedCustomerManagementService.java
 *
 * (c) Copyright iC Consult GmbH, 2021
 * All Rights reserved.
 *
 * iC Consult GmbH
 * 45128 Essen
 * Germany
 *
 */

package com.icconsult.interview.usermanagement.behavior;

import com.icconsult.interview.usermanagement.api.dto.CustomerRequest;
import com.icconsult.interview.usermanagement.api.dto.CustomerResponse;
import com.icconsult.interview.usermanagement.exception.CustomerNotFoundException;
import com.icconsult.interview.usermanagement.persistance.CustomerEntity;
import com.icconsult.interview.usermanagement.persistance.CustomerRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import static com.icconsult.interview.usermanagement.exception.CustomerNotFoundException.CUSTOMER_WITH_ID_NOT_FOUND;


@Service
public class DefaultCustomerManagementService implements CustomerManagementService {

    Logger logger = LoggerFactory.getLogger(DefaultCustomerManagementService.class);

    private final CustomerRepository customerRepository;

    @Autowired
    public DefaultCustomerManagementService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public CustomerResponse getCustomer(String userId) {
        CustomerEntity customerEntity = getCustomerEntityByUserId(userId);
        logger.info("Successfully retrieved customer from: [givenName="
                + anonymizeString(customerEntity.getGivenName()) + ", familyName="
                + anonymizeString(customerEntity.getFamilyName()) + ", email="
                + anonymizeString(customerEntity.getEmail()) + "].");
        return toCustomerResponse(customerEntity);
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(String userId, String admin, CustomerRequest newCustomerEntry) {
        CustomerEntity customerEntity = getCustomerEntityByUserId(userId);
        customerEntity.setFamilyName(newCustomerEntry.getFamilyName());
        customerEntity.setGivenName(newCustomerEntry.getGivenName());
        customerEntity.setEmail(newCustomerEntry.getEmail());


        try {
//            I think its better idea to use transactional for important database operations instead of:
//            customerRepository.save(customerEntity) as in case it was not a looger,
//            if it was some validation functionality, the changes should not be persisted.
            customerRepository.save(customerEntity);
            logger.info("Customer update successful, new values: [givenName="
                    + anonymizeString(customerEntity.getGivenName())
                    + ", familyName="
                    + anonymizeString(customerEntity.getFamilyName())
                    + ", email=" + anonymizeString(customerEntity.getEmail())
                    + "].");
            return toCustomerResponse(customerEntity);
        } catch (Exception e) {
            logger.error("Error while trying to update customer [{}]", e.getMessage(), e);
            throw e;
        }
    }

    private CustomerResponse toCustomerResponse(CustomerEntity customerEntity) {
        return new CustomerResponse(customerEntity.getUserId(), customerEntity.getGivenName(), customerEntity.getFamilyName(), customerEntity.getEmail());
    }

    private CustomerEntity getCustomerEntityByUserId(String userId) {
        return customerRepository.findByUserId(userId).orElseThrow(() ->
                new CustomerNotFoundException(String.format(CUSTOMER_WITH_ID_NOT_FOUND, anonymizeString(userId))));
    }

    private String anonymizeString(String plaintext) {
        if (plaintext == null || plaintext.isBlank() || plaintext.length() <= 2) {
            return plaintext;
        } else {
            String padding = StringUtils.repeat('*', plaintext.length() - 2);
            return plaintext.charAt(0) + padding + plaintext.charAt(plaintext.length() - 1);
        }
    }

}
