package com.wells.gcp.iam;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.cloudresourcemanager.v3.CloudResourceManager;
import com.google.api.services.cloudresourcemanager.v3.model.GetIamPolicyRequest;
import com.google.api.services.cloudresourcemanager.v3.model.Policy;
import com.google.api.services.iam.v1.Iam;
import com.google.api.services.iam.v1.IamScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Test {
    static Iam iamService;
    static CloudResourceManager cloudResourceManager;
    static Policy policy;
    static String PROJECT_ID = "quixotic-height-349700";

    static Services services;

    private static Iam initIamService() throws GeneralSecurityException, IOException {
        GoogleCredentials credential =
                GoogleCredentials.getApplicationDefault()
                        .createScoped(Collections.singleton(IamScopes.CLOUD_PLATFORM));
        // Initialize the IAM service, which can be used to send requests to the IAM API.
        Iam service =
                new Iam.Builder(
                        GoogleNetHttpTransport.newTrustedTransport(),
                        JacksonFactory.getDefaultInstance(),
                        new HttpCredentialsAdapter(credential))
                        .setApplicationName("service-accounts")
                        .build();
        return service;
    }

    public static CloudResourceManager initializeCloudResourceManagerService()
            throws IOException, GeneralSecurityException {
        GoogleCredentials credential =
                GoogleCredentials.getApplicationDefault()
                        .createScoped(Collections.singleton(IamScopes.CLOUD_PLATFORM));

        // Creates the Cloud Resource Manager service object.
        CloudResourceManager service =
                new CloudResourceManager.Builder(
                        GoogleNetHttpTransport.newTrustedTransport(),
                        JacksonFactory.getDefaultInstance(),
                        new HttpCredentialsAdapter(credential))
                        .setApplicationName("iam-quickstart")
                        .build();
        return service;
    }

    public static Policy getPolicy(CloudResourceManager crmService, String projectId) {
        // Gets the project's policy by calling the
        // Cloud Resource Manager Projects API.
        Policy policy = null;
        try {
            GetIamPolicyRequest request = new GetIamPolicyRequest();
            policy = crmService.projects().getIamPolicy("projects/"+projectId, request).execute();
        } catch (IOException e) {
            System.out.println("Unable to get policy: \n" + e.getMessage() + e.getStackTrace());
        }
        return policy;
    }

    public static String createServiceAccount(String serviceAccount) {
        String serviceAccountEmail = services.createServiceAccount(PROJECT_ID, serviceAccount, "from ravi using client libraries");
        if (null != serviceAccountEmail) {
            System.out.println("Service account " + serviceAccountEmail + " is successfully created");
        } else {
            System.out.println("Failed to create account " + serviceAccount);
        }
        return serviceAccountEmail;
    }

    public static void deleteServiceAccount(String serviceAccount) {
        String serviceAccountEmail = serviceAccount + "@" + PROJECT_ID + ".iam.gserviceaccount.com";
        boolean isServiceAccountDeleted= services.deleteServiceAccount(PROJECT_ID,serviceAccountEmail);
        if (isServiceAccountDeleted) {
            System.out.println("Service account " + serviceAccountEmail + " is successfully deleted");
        } else {
            System.out.println("Failed to delete account " + serviceAccountEmail);
        }
    }

    /**
     * Binds given role to service accounts
     * @param serviceAcctEmail
     */
    private static void addBinding(String serviceAcctEmail) {

        List<String> members = new ArrayList<>();
        members.add("serviceAccount:"+serviceAcctEmail);

        String role = "roles/logging.logWriter";

        boolean isBindingAdded= services.addBinding(policy,role,members,PROJECT_ID,cloudResourceManager);

        if (isBindingAdded) {
            System.out.println("Given services accounts are assigned successfully to role " + role);
        } else {
            System.out.println("Failed to assign roles ");
        }
    }

    private static void addMember(String serviceAccountEmail) {
        String role = "roles/logging.logWriter";

        boolean isMemberAdded= services.addMember(cloudResourceManager,policy,role,serviceAccountEmail,PROJECT_ID);

        if (isMemberAdded) {
            System.out.println("Given service account became member successfully to " + role);
        } else {
            System.out.println("Failed to add member ");
        }
    }

    public static void main(String[] args) {
        try {
            iamService = initIamService();
            cloudResourceManager = initializeCloudResourceManagerService();
            policy = getPolicy(cloudResourceManager, PROJECT_ID);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        services = new Services(iamService);
        String serviceAccount = "test-srv-acct-client-api";

        //String serviceAccountEmail = createServiceAccount(serviceAccount);


        addMember("test-srv-acct-client-api@quixotic-height-349700.iam.gserviceaccount.com");

        //deleteServiceAccount(serviceAccount);


    }




}
