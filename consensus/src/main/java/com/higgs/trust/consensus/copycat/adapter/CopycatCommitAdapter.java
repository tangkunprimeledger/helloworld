package com.higgs.trust.consensus.copycat.adapter;

import com.higgs.trust.consensus.core.ConsensusCommit;
import io.atomix.copycat.Operation;
import io.atomix.copycat.server.Commit;
import io.atomix.copycat.server.session.ServerSession;

import java.time.Instant;

/**
 * @author cwy
 */
public class CopycatCommitAdapter<T extends Operation> implements Commit<T>, ConsensusCommit<T> {

    private Commit<T> commit;

    public CopycatCommitAdapter(Object obj) {
        if (obj instanceof Commit) {
            this.commit = (Commit<T>) obj;
        } else {
            throw new RuntimeException("the commit is not support!");
        }
    }

    @Override
    public long index() {
        return commit.index();
    }

    @Override
    public ServerSession session() {
        return commit.session();
    }

    @Override
    public Instant time() {
        return commit.time();
    }

    @Override
    public Class<T> type() {
        return commit.type();
    }

    @Override
    public T operation() {
        return commit.operation();
    }

    @Override
    public T command() {
        return commit.command();
    }

    @Override
    public T query() {
        return commit.query();
    }

    @Override
    public Commit<T> acquire() {
        return commit.acquire();
    }

    @Override
    public boolean release() {
        return commit.release();
    }

    @Override
    public int references() {
        return commit.references();
    }

    @Override
    public void close() {
        commit.close();
    }
}
