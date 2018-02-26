#!/usr/bin/perl

use warnings;
use strict;

# Takes an RPSL object stream on stdin and produces SQL on stdout that
# can be used to populate a MySQL database.  That database can then in
# turn be used to seed an instance of rdapd (current-state only).

my %OBJECT_TYPE_TO_ID = (
    'domain'       => 3,
    'inetnum'      => 6,
    'key-cert'     => 7,
    'filter-set'   => 14,
    'mntner'       => 9,
    'person'       => 10,
    'role'         => 11,
    'organisation' => 18,
    'route-set'    => 13,
    'as-set'       => 1,
    'inet6num'     => 5,
    'inet-rtr'     => 4,
    'as-block'     => 0,
    'aut-num'      => 2,
    'limerick'     => 8,
    'route'        => 12,
    'route6'       => 19,
    'peering-set'  => 15,
    'rtr-set'      => 16,
    'irt'          => 17,
    'poem'         => 20,
    'poetic-form'  => 21,
);

sub trim
{
    my ($value) = @_;

    $value =~ s/^\s*//;
    $value =~ s/\s*$//;

    return $value;
}

sub get_attr
{
    my ($object, $attr_name) = @_;

    for my $attr (@{$object}) {
        if ($attr->[0] eq $attr_name) {
            return $attr->[1];
        }
    }

    return;
}

sub arrayify
{
    my $string = shift;

    my $index  = 0;
    my $record = [];

    foreach my $line (split /\r?\n+/, $string) {
        if ($line =~ m/^[ \t\+]/) {
            $line =~ s/^\s*/\n/;
            $record->[$index - 1]->[1] .= $line if ($index > 0);
        } elsif ($line =~ m/^([A-Za-z0-9\-]+):\s*(.*)$/) {
            $record->[$index++] = [$1, $2];
        }
    }

    return $record;
}

sub _pkey_normalise
{
    my ($pkey) = @_;

    $pkey =~ s/\t/ /g;
    $pkey =~ s/\s+/ /g;

    return $pkey;
}

sub whois_object_to_pkey
{
    my ($object) = @_;

    my ($type, $type_attr) = @{$object->[0]};
    $type_attr = trim($type_attr);

    my $pkey =
        ($type eq 'route' or $type eq 'route6')
            ? _pkey_normalise($type_attr.(trim(get_attr($object, 'origin'))))
      : ($type eq 'person' or $type eq 'role')
            ? (get_attr($object, 'nic-hdl'))
                ? _pkey_normalise(trim(get_attr($object, 'nic-hdl')))
                : $type_attr
            : _pkey_normalise($type_attr);

    return $pkey;
}

print <<EOF;
DROP TABLE IF EXISTS last;
DROP TABLE IF EXISTS history;
DROP TABLE IF EXISTS serials;

CREATE TABLE `history` (
  `thread_id` int(11) NOT NULL DEFAULT '0',
  `object_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `sequence_id` int(10) unsigned NOT NULL DEFAULT '1',
  `timestamp` int(10) unsigned NOT NULL DEFAULT '0',
  `object_type` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `object` longblob NOT NULL,
  `pkey` varchar(254) NOT NULL DEFAULT '',
  `serial` int(11) NOT NULL DEFAULT '0',
  `prev_serial` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`object_id`,`sequence_id`),
  KEY `last_pkey` (`pkey`),
  KEY `object_type_index` (`object_type`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `last` (
  `thread_id` int(11) NOT NULL DEFAULT '0',
  `object_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `sequence_id` int(10) unsigned NOT NULL DEFAULT '1',
  `timestamp` int(10) unsigned NOT NULL DEFAULT '0',
  `object_type` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `object` longblob NOT NULL,
  `pkey` varchar(254) NOT NULL DEFAULT '',
  `serial` int(11) NOT NULL DEFAULT '0',
  `prev_serial` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`object_id`,`sequence_id`),
  KEY `last_pkey` (`pkey`),
  KEY `object_type_index` (`object_type`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `serials` (
  `thread_id` int(10) unsigned NOT NULL DEFAULT '0',
  `serial_id` int(11) NOT NULL AUTO_INCREMENT,
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `sequence_id` int(10) unsigned NOT NULL DEFAULT '0',
  `atlast` tinyint(4) unsigned NOT NULL DEFAULT '0',
  `operation` tinyint(4) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`serial_id`),
  KEY `object` (`object_id`,`sequence_id`),
  KEY `thread_id` (`thread_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
EOF

$/ = "\n\n";
while (my $record = <>) {
    chomp $record;
    $record =~ s/"/\\"/g;
    my $object = arrayify($record);
    my $object_type = $object->[0]->[0];
    my $object_type_id = $OBJECT_TYPE_TO_ID{$object_type};
    my $pkey = whois_object_to_pkey($object);
    print qq{INSERT INTO last (thread_id, object_id, sequence_id, timestamp, object_type, object, pkey, serial, prev_serial) VALUES (0, NULL, 1, NOW(), $object_type_id, "$record", "$pkey", 0, 0);};
    print "\n";
    print qq{INSERT INTO serials (thread_id, serial_id, object_id, sequence_id, atlast, operation) VALUES (0, NULL, LAST_INSERT_ID(), 1, 0, 0);};
    print "\n";
}

1;
