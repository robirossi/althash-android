package org.qtum.wallet.ui.fragment.token_fragment;

import org.qtum.wallet.datastorage.HistoryList;
import org.qtum.wallet.datastorage.TokenHistoryList;
import org.qtum.wallet.model.contract.Token;
import org.qtum.wallet.model.gson.token_history.TokenHistory;
import org.qtum.wallet.model.gson.token_history.TokenHistoryResponse;
import org.qtum.wallet.ui.base.base_fragment.BaseFragmentPresenterImpl;
import org.qtum.wallet.ui.fragment.wallet_fragment.WalletInteractorImpl;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.internal.util.SubscriptionList;
import rx.schedulers.Schedulers;

public class TokenPresenterImpl extends BaseFragmentPresenterImpl implements TokenPresenter {

    private TokenView view;
    private TokenInteractor interactor;
    private Token token;
    private String abi;
    private SubscriptionList mSubscriptionList = new SubscriptionList();

    private final int ONE_PAGE_COUNT = 25;

    public TokenPresenterImpl(TokenView view, TokenInteractor interactor) {
        this.view = view;
        this.interactor = interactor;
    }

    @Override
    public void initializeViews() {
        super.initializeViews();
        setQtumAddress();

        if (token.getDecimalUnits() == null) {
            getInteractor().setupPropertyDecimalsValue(token, getView().getDecimalsValueCallback());
        } else {
            getView().onContractPropertyUpdated(TokenFragment.decimals, String.valueOf(token.getDecimalUnits()));
            getView().setBalance(token.getTokenBalanceWithDecimalUnits().toString());
            getInteractor().setupPropertyTotalSupplyValue(token, getView().getTotalSupplyValueCallback());
        }

        getInteractor().setupPropertySymbolValue(token, getView().getSymbolValueCallback());
        getInteractor().setupPropertyNameValue(token, getView().getNameValueCallback());

        getView().updateHistory(getInteractor().getHistoryList());
    }

    @Override
    public Token getToken() {
        return token;
    }

    public String getAbi() {
        abi = (getView().isAbiEmpty(abi)) ? getInteractor().readAbiContract(token.getUiid()) : abi;
        return abi;
    }

    @Override
    public void setToken(Token token) {
        this.token = token;
    }

    private void setQtumAddress() {
        getView().setQtumAddress(getInteractor().getCurrentAddress());
    }

    @Override
    public TokenView getView() {
        return view;
    }

    public TokenInteractor getInteractor() {
        return interactor;
    }

    @Override
    public void onDecimalsPropertySuccess(String value) {
        token = getInteractor().setTokenDecimals(token, value);
        getView().setBalance(token.getTokenBalanceWithDecimalUnits().toString());
        getInteractor().setupPropertyTotalSupplyValue(token, getView().getTotalSupplyValueCallback());
    }

    @Override
    public String onTotalSupplyPropertySuccess(Token token, String value) {
        return getInteractor().handleTotalSupplyValue(token, value);
    }

    public void setAbi(String abi) {
        this.abi = abi;
    }

    @Override
    public void onLastItem(final int currentItemCount) {
        if (getInteractor().getHistoryList().size() != getInteractor().getTotalHistoryItem()) {
            //getView().loadNewHistory();
            mSubscriptionList.add(getInteractor().getHistoryList(token.getContractAddress(), ONE_PAGE_COUNT, currentItemCount)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<TokenHistoryResponse>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(TokenHistoryResponse historyResponse) {

                            TokenHistoryList.newInstance().getTokenHistories().addAll(historyResponse.getItems());
                            //getView().addHistory(currentItemCount, getInteractor().getHistoryList().size() - currentItemCount + 1,
                            //        getInteractor().getHistoryList());
                        }
                    }));

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mSubscriptionList != null) {
            mSubscriptionList.clear();
        }
    }
}