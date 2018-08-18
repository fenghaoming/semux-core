/*
 * Copyright (c) [2018] [ The Semux Developers ]
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.vm.client;

import java.math.BigInteger;

import org.ethereum.vm.util.ByteArrayUtil;

/**
 * A facade interface for Transaction. The client needs to wrap the native
 * transaction to comply this specification, in order to use EVM.
 */
public interface Transaction {

    /**
     * Returns whether this transaction is a CREATE.
     *
     * @return true if it's CREATE.
     */
    boolean isCreate();

    /**
     * Returns the address.
     *
     * @return a 20-byte array, not NULL.
     */
    byte[] getFrom();

    /**
     * Returns the recipient address.
     *
     * @return a 20-byte array, or {@link ByteArrayUtil#EMPTY_BYTE_ARRAY} for
     *         CREATE, not NULL.
     */
    byte[] getTo();

    /**
     * Returns the nonce of the sender.
     *
     * @return the nonce
     */
    long getNonce();

    /**
     * Returns the value being transferred.
     *
     * @return the value with a decimal of <em>18</em>, not NULL.
     */
    BigInteger getValue();

    /**
     * Returns the data field.
     *
     * @return the call data, not NULL.
     */
    byte[] getData();

    /**
     * Returns the gas limit. Because an invalid internal transaction will set the
     * gas limit larger than {@link Long#MAX_VALUE}, this value is represented in
     * BigInt.
     *
     * @return the specified gas for this transaction.
     */
    BigInteger getGas();

    /**
     * Returns the gas price.
     *
     * @return the specified gas price with a decimal of <em>18</em>, not NULL.
     */
    BigInteger getGasPrice();
}
