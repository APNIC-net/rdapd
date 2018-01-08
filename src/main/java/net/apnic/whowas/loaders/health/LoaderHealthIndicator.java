package net.apnic.whowas.loaders.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public class LoaderHealthIndicator
    implements HealthIndicator
{
    private boolean finishedLoading = false;

    @Override
    public Health health()
    {
        return (finishedLoading ? Health.up() : Health.down()).build();
    }

    public void setFinishedLoading()
    {
        finishedLoading = true;
    }
}
