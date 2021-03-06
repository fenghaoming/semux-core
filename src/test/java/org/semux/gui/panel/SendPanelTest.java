/**
 * Copyright (c) 2017-2020 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.gui.panel;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.semux.core.Unit.SEM;

import org.apache.commons.lang3.RandomUtils;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.assertj.swing.timing.Timeout;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.semux.KernelMock;
import org.semux.core.Amount;
import org.semux.core.Blockchain;
import org.semux.core.PendingManager;
import org.semux.core.Transaction;
import org.semux.core.TransactionResult;
import org.semux.core.TransactionType;
import org.semux.core.state.AccountState;
import org.semux.crypto.Hex;
import org.semux.crypto.Key;
import org.semux.gui.WalletModelRule;
import org.semux.message.GuiMessages;
import org.semux.rules.KernelRule;

@RunWith(MockitoJUnitRunner.Silent.class)
public class SendPanelTest extends AssertJSwingJUnitTestCase {

    @Rule
    public KernelRule kernelRule1 = new KernelRule(51610, 51710);

    @Rule
    public WalletModelRule walletRule = new WalletModelRule(Amount.of(1000, SEM), Amount.of(1000, SEM));

    @Mock
    PendingManager pendingManager;

    @Mock
    Blockchain blockchain;

    @Mock
    AccountState accountState;

    @Captor
    ArgumentCaptor<Transaction> transactionArgumentCaptor = ArgumentCaptor.forClass(Transaction.class);

    SendPanelTestApplication application;

    FrameFixture window;

    Key recipient;

    KernelMock kernelMock;

    @Override
    protected void onSetUp() {
        recipient = new Key();
    }

    @Override
    protected void onTearDown() {
        Mockito.reset(kernelMock);
    }

    @Test
    public void testSendSuccessfully() {
        testSend(100, new PendingManager.ProcessingResult(1));

        // 1. a confirmation dialog should be displayed
        window.optionPane(Timeout.timeout(1000)).requireTitle(GuiMessages.get("ConfirmTransfer")).requireVisible()
                .yesButton().requireVisible().click();

        // 2. filled transaction should be sent to PendingManager once "Yes" button is
        // clicked
        window.optionPane(Timeout.timeout(1000)).requireTitle(GuiMessages.get("SuccessDialogTitle")).requireVisible()
                .okButton().requireVisible().click();
        verify(pendingManager).addTransactionSync(transactionArgumentCaptor.capture());

        // verify transaction
        Transaction tx = transactionArgumentCaptor.getValue();
        assertEquals(TransactionType.TRANSFER, tx.getType());
        assertArrayEquals(recipient.toAddress(), tx.getTo());
        assertEquals(Amount.of(100, SEM), tx.getValue());
        assertEquals(kernelMock.getConfig().spec().minTransactionFee(), tx.getFee());
    }

    @Test
    public void testSendFailure() {
        testSend(10000, new PendingManager.ProcessingResult(0, TransactionResult.Code.INSUFFICIENT_AVAILABLE));
        window.optionPane(Timeout.timeout(1000)).requireTitle(GuiMessages.get("ErrorDialogTitle"));
    }

    @Test
    public void testSendFailureInvalidInput() {
        testSend(null, null);
        window.optionPane(Timeout.timeout(1000)).requireVisible().requireMessage(GuiMessages.get("EnterValidValue"));
    }

    private void testSend(Integer toSendSEM, PendingManager.ProcessingResult mockResult) {
        kernelMock = spy(kernelRule1.getKernel());
        application = GuiActionRunner.execute(() -> new SendPanelTestApplication(walletRule.walletModel, kernelMock));

        // mock pending manager
        when(kernelMock.getPendingManager()).thenReturn(pendingManager);
        when(pendingManager.getNonce(any())).thenReturn(RandomUtils.nextLong());
        when(pendingManager.addTransactionSync(any())).thenReturn(mockResult);

        // mock blockchain
        when(kernelMock.getBlockchain()).thenReturn(blockchain);
        when(blockchain.getAccountState()).thenReturn(accountState);
        when(accountState.getCode(any())).thenReturn(null);

        // create window
        window = new FrameFixture(robot(), application);
        window.show().requireVisible().moveToFront();

        // fill form
        window.comboBox("selectTo").requireVisible().requireEditable().enterText(Hex.encode0x(recipient.toAddress()));
        window.textBox("txtValue").requireVisible().requireEditable()
                .setText(toSendSEM == null ? "" : String.valueOf(toSendSEM))
                .requireText(toSendSEM == null ? "" : String.valueOf(toSendSEM));
        window.button("btnSend").requireVisible().click();
    }
}
