/*
 * Copyright 2013 Basho Technologies Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.basho.riak.client.operations;

import com.basho.riak.client.cap.Quorum;
import com.basho.riak.client.cap.VClock;
import com.basho.riak.client.core.RiakCluster;
import com.basho.riak.client.core.operations.DeleteOperation;
import com.basho.riak.client.query.KvResponse;
import com.basho.riak.client.util.ByteArrayWrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class DeleteValue implements RiakCommand<DeleteValue.Response>
{

    private final RiakCluster cluster;
    private final Key location;
    private final Map<DeleteOption<?>, Object> options;
    private VClock vClock;

    DeleteValue(RiakCluster cluster, Key location)
    {
        this.cluster = cluster;
        this.location = location;
        this.options = new HashMap<DeleteOption<?>, Object>();
    }

    @Override
    public Response execute() throws ExecutionException, InterruptedException
    {

        ByteArrayWrapper type = location.getType();
        ByteArrayWrapper bucket = location.getBucket();
        ByteArrayWrapper key = location.getKey();

        DeleteOperation.Builder builder = new DeleteOperation.Builder(bucket, key);

        if (type != null)
        {
            builder.withBucketType(type);
        }

        for (Map.Entry<DeleteOption<?>, Object> optPair : options.entrySet())
        {

            DeleteOption<?> option = optPair.getKey();

            if (option == DeleteOption.DW)
            {
                builder.withDw(((Quorum) optPair.getValue()).getIntValue());
            }
            else if (option == DeleteOption.N_VAL)
            {
                builder.withNVal(((Quorum) optPair.getValue()).getIntValue());
            }
            else if (option == DeleteOption.PR)
            {
                builder.withPr(((Quorum) optPair.getValue()).getIntValue());
            }
            else if (option == DeleteOption.R)
            {
                builder.withR(((Quorum) optPair.getValue()).getIntValue());
            }
            else if (option == DeleteOption.PW)
            {
                builder.withPw(((Quorum) optPair.getValue()).getIntValue());
            }
            else if (option == DeleteOption.RW)
            {
                builder.withRw(((Quorum) optPair.getValue()).getIntValue());
            }
            else if (option == DeleteOption.SLOPPY_QUORUM)
            {
                builder.withSloppyQuorum((Boolean) optPair.getValue());
            }
            else if (option == DeleteOption.TIMEOUT)
            {
                builder.withTimeout((Integer) optPair.getValue());
            }
            else if (option == DeleteOption.W)
            {
                builder.withW(((Quorum) optPair.getValue()).getIntValue());
            }
        }

        DeleteOperation operation = builder.build();
        cluster.execute(operation);

        KvResponse<Boolean> response = operation.get();

        return new Response(true);

    }

    public DeleteValue withVClock(VClock vClock)
    {
        this.vClock = vClock;
        return this;
    }

    public <T> DeleteValue withOption(DeleteOption<T> option, T value)
    {
        options.put(option, value);
        return this;
    }

    public static class Response
    {
        private final boolean deleted;

        public Response(boolean deleted)
        {
            this.deleted = deleted;
        }

        public boolean isDeleted()
        {
            return deleted;
        }
    }

}