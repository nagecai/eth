package eth;

import eth.client.Web3JClient;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.core.*;
import org.web3j.protocol.core.methods.request.*;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;
import rx.Subscription;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.List;

/**
 * Created by Administrator on 2018/4/9.
 */
public class MyWeb3j {

    private static Web3j web3 = Web3JClient.getClient();

    private static String walletParentPath = "D:/eth/wallet/";

    // base
//    private static String walletFileName = "UTC--2018-02-26T10-04-34.233627776Z--cc3c0e436345d5e3ece7492a236deb66799cf9e7";

    private static String walletFileName = "UTC--2018-04-08T08-04-17.560150766Z--78c001ad4409ceb94dc0e07e1f2cd9ac62f24d10";

    private static BigInteger GAS_PRICE = Contract.GAS_PRICE;

    private static BigInteger GAS_LIMIT = Contract.GAS_LIMIT;

    public static void main(String[] args) {
        try {
            // version
            final String version = getVersion();
            System.out.println("Version: " +version);


//            blockSubscription();
//            transactionSubscription();
//            pendingTransactionSubscription();


            // create new wallet address
//            String walletName = createWalletAddr("123", walletParentPath);
//            System.out.println("New wallet file Name : " + walletName);


            // load wallet file
            Credentials credentials = loadWallet("123", walletParentPath + walletFileName);
            System.out.println("钱包地址: " + credentials.getAddress());
//            System.out.println("私钥: " + credentials.getEcKeyPair().getPrivateKey());
//            System.out.println("公钥: " + credentials.getEcKeyPair().getPublicKey());


//            showAllAccounts();

            // unlock account
//            boolean locked = unLockAccount(credentials.getAddress(), "123");
//            System.out.println( locked ? "Account Unlocked!" : "Unlocked failed!");

            //get balance
//            BigInteger balance = getBalance(credentials.getAddress());
//            System.out.println("Account Balance: " + balance);




            // get Nonce
            BigInteger nonce = getNonce(credentials.getAddress());
            System.out.println("Nonce: " + nonce);

            //transactionV2
//            transactionV2(credentials, nonce, GAS_PRICE, GAS_LIMIT, "0x43cde763f55a9730da95bc092cf206cdfa7e64a4", 1L);

            // offline transaction
//            transaction(credentials, "0x43cde763f55a9730da95bc092cf206cdfa7e64a4", 1L);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void transactionV2(Credentials credentials, BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String toAddress, Long num) throws IOException {
        BigInteger value = Convert.toWei(num + "", Convert.Unit.ETHER).toBigInteger();
        RawTransaction rawTransaction =  createEtherTransaction(nonce, gasPrice, gasLimit, toAddress, value);
        // signe
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String hexValue = Numeric.toHexString(signedMessage);

        EthSendTransaction ethSendTransaction = web3.ethSendRawTransaction(hexValue).send();
        String transactionHash = ethSendTransaction.getTransactionHash();
        System.out.println(transactionHash);
    }

    private static RawTransaction createEtherTransaction(BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String address, BigInteger num) {
        return RawTransaction.createEtherTransaction(nonce, gasPrice, gasLimit, address, num);
    }

    private static void startContract(Credentials credentials, BigInteger num) throws Exception {

//        String ownAddress = "0xD1c82c71cC567d63Fd53D5B91dcAC6156E5B96B3";
//        String toAddress = "0x6e27727bbb9f0140024a62822f013385f4194999";
//        //部署智能合约
//        Greeter greeter = Greeter.deploy(web3, credentials, GAS_PRICE, GAS_LIMIT, num).send();
//        System.out.println(greeter.getContractAddress());
//        //调用智能合约
//        System.out.println(greeter.greet().send());
    }

    private static BigInteger getBlockNumber() throws IOException {
        EthBlockNumber ethBlockNumber = web3.ethBlockNumber().send();
        BigInteger blockNumber = ethBlockNumber.getBlockNumber();
        System.out.println("blockNumber: " + blockNumber);
        return blockNumber;
    }

    private static BigInteger  getNonce(String address) throws IOException {
//        DefaultBlockParameter dbp = DefaultBlockParameter.valueOf("latest");
        DefaultBlockParameter dbp = new DefaultBlockParameterNumber(getBlockNumber());
//        DefaultBlockParameter dbp = DefaultBlockParameterName.LATEST;
        EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(address, dbp).send();
        return ethGetTransactionCount.getTransactionCount();
    }

    private static Transaction createFunctionTransaction(String fromAddress, BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String to, BigInteger num, String hex) {
        return Transaction.createFunctionCallTransaction(fromAddress, nonce, gasPrice, gasLimit, to, num, hex);
    }


    /**
     *
     * recommended transaction
     *
     * @param credentials
     * @param to
     * @param num
     */
    private static void transaction(Credentials credentials, String to, Long num) {
            try {
                TransactionReceipt transactionReceipt = Transfer.sendFunds(web3, credentials, to,
                        BigDecimal.valueOf(num), Convert.Unit.ETHER)
                        .send();

                String transactionHash = transactionReceipt.getTransactionHash();
                System.out.println("TransactionHash: " + transactionHash);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    private static Boolean unLockAccount(String walletAddress, String password) throws IOException {
        Admin admin = Admin.build(new HttpService(Web3JClient.ip));
        PersonalUnlockAccount personalUnlockAccount = admin.personalUnlockAccount(walletAddress, password).send();
        return personalUnlockAccount.accountUnlocked();
    }

    /**
     * get Balance
     * @param walletAddress
     * @return
     * @throws IOException
     */
    private static BigInteger getBalance(String walletAddress) throws IOException {
        DefaultBlockParameter dbp = DefaultBlockParameter.valueOf("latest");
        EthGetBalance balance = web3.ethGetBalance(walletAddress, dbp).send();
        return balance.getBalance();
    }

    private static List<String> getAccounts() throws IOException {
        return web3.ethAccounts().send().getAccounts();
    }

    private static void showAllAccounts() throws IOException {
        List<String> accounts = web3.ethAccounts().send().getAccounts();
        DefaultBlockParameter dbp = DefaultBlockParameter.valueOf("latest");
        System.out.println("Accounts list: ");
        for(String ac : accounts){
            EthGetBalance balance = web3.ethGetBalance(ac, dbp).send();
            System.out.println(ac + " : " + balance.getBalance());
        }
    }


    /**
     *  load wallet file
     */
    private static Credentials loadWallet(String password, String walletAddress) {
        try {
            return WalletUtils.loadCredentials(password, walletAddress);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CipherException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     *  create wallet file
     */
    private static String createWalletAddr(String password, String parentPath) {
        try {
            return WalletUtils.generateNewWalletFile(password, new File(parentPath), false);
        } catch (CipherException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * To receive all pending transactions as they are submitted to the network
     * (i.e. before they have been grouped into a block together)
     */
    private static void pendingTransactionSubscription() {
        Subscription pendingTransactionSubscription = web3.pendingTransactionObservable().subscribe(transaction -> {
            final String to = transaction.getTo();
            final String from = transaction.getFrom();
            final BigInteger value = transaction.getValue();
            System.out.println("Pre to Transaction: " + from + "  -->  " + to + "  :  " + value);
        });

//        pendingTransactionSubscription.unsubscribe();
    }

    /**
     * To receive all new transactions as they are added to the blockchain
     */
    private static void transactionSubscription() {
        Subscription transactionSubscription = web3.transactionObservable().subscribe(transaction -> {
            transaction.getBlockNumber();
            final String to = transaction.getTo();
            final String from = transaction.getFrom();
            final BigInteger value = transaction.getValue();
            System.out.println("Transaction: " + from + "  -->  " + to + "  :  " + value);
        });

//        transactionSubscription.unsubscribe();
    }

    /**
        To receive all new blocks as they are added to the blockchain
     */
    private static void blockSubscription() {
        Subscription blockSubscription = web3.blockObservable(false).subscribe(block -> {
            System.out.println(block.getBlock());
        });

//        blockSubscription.unsubscribe();
    }

    private static String getVersion() throws IOException {
        Web3ClientVersion web3ClientVersion = web3.web3ClientVersion().send();
        return web3ClientVersion.getWeb3ClientVersion();
    }
}
