/*
 *  Copyright 1999-2019 Seata.io Group.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.zeewain.socket.netty.compress;


/**
 * 这个实现是为了方便调用
 */
public class UnCompressor implements Compressor {

    public UnCompressor() {
        Compressor.register(getType(), this);
    }

    @Override
    public byte getType() {
        return UN_COMPRESS;
    }

    @Override
    public byte[] compress(byte[] src) {
        return src;
    }

    @Override
    public byte[] uncompress(byte[] src) {
        return src;
    }

}