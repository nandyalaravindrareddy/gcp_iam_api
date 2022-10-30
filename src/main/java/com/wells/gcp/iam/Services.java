package com.wells.gcp.iam;

import com.google.api.services.cloudresourcemanager.v3.CloudResourceManager;
import com.google.api.services.cloudresourcemanager.v3.model.Binding;
import com.google.api.services.cloudresourcemanager.v3.model.Policy;
import com.google.api.services.cloudresourcemanager.v3.model.SetIamPolicyRequest;
import com.google.api.services.iam.v1.Iam;
import com.google.api.services.iam.v1.model.CreateServiceAccountRequest;
import com.google.api.services.iam.v1.model.ServiceAccount;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class Services {
    public Services() {
    }

    public Services(Iam iamService) {
        this.iamService = iamService;
    }

    private Iam iamService;

    public Iam getIamService() {
        return iamService;
    }

    public void setIamService(Iam iamService) {
        this.iamService = iamService;
    }

    public String createServiceAccount(String projectId, String serviceAccountName, String displayName) {
        String serviceAccountEmail = "";
        try {
            ServiceAccount serviceAccount = new ServiceAccount();
            serviceAccount.setDisplayName(displayName);
            CreateServiceAccountRequest request = new CreateServiceAccountRequest();
            request.setAccountId(serviceAccountName);
            request.setServiceAccount(serviceAccount);

            serviceAccount =
                    iamService.projects().serviceAccounts().create("projects/" + projectId, request).execute();
            serviceAccountEmail = serviceAccount.getEmail();
            System.out.println("Created service account: " + serviceAccountEmail);
        } catch (IOException e) {
            System.out.println("Unable to create service account: \n" + e.toString());
        }
        return serviceAccountEmail;
    }

    public boolean deleteServiceAccount(String projectId, String serviceAccountEmail) {
        boolean isServiceAccountDeleted = false;
        try {
            iamService
                    .projects()
                    .serviceAccounts()
                    .delete("projects/-/serviceAccounts/" + serviceAccountEmail)
                    .execute();

            System.out.println("Deleted service account: " + serviceAccountEmail);
            isServiceAccountDeleted = true;
        } catch (IOException e) {
            System.out.println("Unable to delete service account: \n" + e.toString());
        }
        return isServiceAccountDeleted;
    }

    public boolean addBinding(Policy policy, String role, List<String> members,String projectId,CloudResourceManager cloudResourceManager) {
        boolean isBindingAdded = false;
        try{
            Binding binding = new Binding();
            binding.setRole(role);
            binding.setMembers(members);

            policy.getBindings().add(binding);

            setPolicy(cloudResourceManager, projectId, policy);

            System.out.println("Added binding: " + binding.toString());
            isBindingAdded = true;
        }catch (Exception e){
            System.out.println("binding failed");
        }
        return isBindingAdded;
    }


    public boolean addMember(CloudResourceManager cloudResourceManager,Policy policy,String role,String member,String projectId) {
        List<Binding> bindings = policy.getBindings();
        boolean isMemberAdded = false;
        for (Binding b : bindings) {
            if (b.getRole().equals(role)) {
                b.getMembers().add("serviceAccount:"+member);
                System.out.println("Member " + member + " added to role " + role);
                setPolicy(cloudResourceManager,projectId , policy);
                isMemberAdded = true;
                break;
            }
        }
        System.out.println("Role not found in policy; member not added");
        return isMemberAdded;
    }

    public static boolean removeMember(CloudResourceManager cloudResourceManager,Policy policy,String role,String member,String projectId) {
        boolean isMemberAdded = false;
        List<Binding> bindings = policy.getBindings();
        Binding binding = null;
        for (Binding b : bindings) {
            if (b.getRole().equals(role)) {
                binding = b;
            }
        }
        if (binding.getMembers().contains(member)) {
            binding.getMembers().remove(member);
            System.out.println("Member " + member + " removed from " + role);
            if (binding.getMembers().isEmpty()) {
                policy.getBindings().remove(binding);
            }
            setPolicy(cloudResourceManager,projectId , policy);
            isMemberAdded = true;
        }

        System.out.println("Role not found in policy; member not removed");
        return isMemberAdded;
    }


    private static void setPolicy(CloudResourceManager crmService, String projectId, Policy policy) {
        // Sets the project's policy by calling the
        // Cloud Resource Manager Projects API.
        try {
            SetIamPolicyRequest request = new SetIamPolicyRequest();
            request.setPolicy(policy);
            crmService.projects().setIamPolicy("projects/"+projectId, request).execute();
        } catch (IOException e) {
            System.out.println("Unable to set policy: \n" + e.getMessage() + e.getStackTrace());
        }
    }

}
