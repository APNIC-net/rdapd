
INSERT INTO rdapd_test.last (object_id, sequence_id, timestamp, object_type,
                              object, pkey)
VALUES (1, 1, 1329434477, 2, 'aut-num:        AS4617
as-name:        EXAMP1AS
descr:          Example 1
country:        AU
admin-c:        EX1-AP
tech-c:         EX1-AP
notify:         admin@example1.noexist
mnt-by:         MAINT-EXAMPLE-1-AP
mnt-irt:        IRT-EXAMPLE-1-AU
mnt-routes:     MAINT-EXAMPLE-1-AP
changed:        admin@example1.noexist 20170319
changed:        hm-changed@apnic.net 20170322
source:         APNIC', 'AS4617'),

(2, 1, 1329434477, 2, 'aut-num:        AS4623
as-name:        EXAMP2AS
descr:          Example 2
country:        AU
admin-c:        EX2-AP
tech-c:         EX2-AP
notify:         admin@example2.noexist
mnt-by:         MAINT-EXAMPLE-2-AP
mnt-irt:        IRT-EXAMPLE-2-AU
source:         APNIC
changed:        hm-changed@apnic.net 20170324', 'AS4623'),

(3, 1, 1329434477, 5, 'inet6num:        2001:200:122::/48
netname:      EXAMP1IPV6
country:      AU
admin-c:      BWE1-AP
tech-c:       EX1-AP
status:       ASSIGNED NON-PORTABLE
mnt-by:       MAINT-EXAMPLE-1-AP
mnt-irt:      IRT-EXAMPLE-1-AU
changed:      admin@example1.noexist 20170319
changed:      hm-changed@apnic.net 20170313
source:       APNIC', '2001:200:122::/48'),

(4, 1, 1329434477, 5, 'inet6num:        2001:200:102::/48
netname:      EXAMP2IPV6
country:      AU
admin-c:      TSE2-AP
tech-c:       EX2-AP
status:       ASSIGNED NON-PORTABLE
mnt-by:       MAINT-EXAMPLE-2-AP
mnt-irt:      IRT-EXAMPLE-2-AU
changed:      admin@example2.noexist 20170321
source:       APNIC', '2001:200:102::/48'),

(5, 1, 1329434477, 6, 'inetnum:        202.75.0.0 - 202.75.3.255
netname:        EXAMP1
country:        AU
admin-c:        BWE1-AP
tech-c:         EX1-AP
mnt-by:         MAINT-EXAMPLE-1-AP
status:         ALLOCATED PORTABLE
mnt-irt:        IRT-EXAMPLE-1-AU
changed:        hm-changed@apnic.net 20170316
source:         APNIC', '202.75.0.0 - 202.75.3.255'),

(6, 1, 1329434477, 6, 'inetnum:        203.127.196.0 - 203.127.196.255
netname:        EXAMP2
country:        AU
admin-c:        TSE2-AP
tech-c:         EX2-AP
mnt-by:         MAINT-EXAMPLE-2-AP
status:         ASSIGNED NON-PORTABLE
source:         APNIC
changed:        hm-changed@apnic.net 20170316',
'203.127.196.0 - 203.127.196.255'),

(7, 1, 1329434477, 3, 'domain:        0.75.202.in-addr.arpa
descr:          in-addr.arpa zone for 202.75.0/22
admin-c:        BWE1-AP
tech-c:         EX1-AP
zone-c:         BWE1-AP
nserver:        ns1.example1.noexist
nserver:        ns2.example1.noexist
mnt-by:         MAINT-EXAMPLE-1-AP
changed:        hm-changed@apnic.net 20170202
source:         APNIC',
'0.75.202.in-addr.arpa'),

(8, 1, 1329434477, 3, 'domain:        2.2.1.0.0.2.1.0.0.2.ip6.arpa
admin-c:        BWE1-AP
tech-c:         EX1-AP
zone-c:         BWE1-AP
nserver:        ns1.example1.noexist
nserver:        ns2.example1.noexist
mnt-by:         MAINT-EXAMPLE-1-AP
changed:        hm-changed@apnic.net 20170202
source:         APNIC',
'2.2.1.0.0.2.1.0.0.2.ip6.arpa'),

(9, 1, 1329434477, 3, 'domain:        196.127.203.in-addr.arpa
descr:          in-addr.arpa zone for 203.127.196/24
admin-c:        TSE2-AP
tech-c:         EX2-AP
zone-c:         TSE2-AP
nserver:        ns1.example2.noexist
nserver:        ns2.example2.noexist
mnt-by:         MAINT-EXAMPLE-2-AP
changed:        hm-changed@apnic.net 20170202
source:         APNIC',
'203.127.196.in-addr.arpa'),

(10, 1, 1329434477, 3, 'domain:        2.0.1.0.0.2.1.0.0.2.ip6.arpa
admin-c:        TSE2-AP
tech-c:         EX2-AP
zone-c:         TSE2-AP
nserver:        ns1.example2.noexist
nserver:        ns2.example2.noexist
mnt-by:         MAINT-EXAMPLE-2-AP
changed:        hm-changed@apnic.net 20170202
source:         APNIC',
'2.0.1.0.0.2.1.0.0.2.ip6.arpa'),

(11, 1, 1329434477, 9, 'mntner:        MAINT-EXAMPLE-1-AP
upd-to:         hostmaster@example1.noexist
descr:          Example 1
admin-c:        BWE1-AP
tech-c:         BWE1-AP
referral-by:    APNIC-HM
auth:           MD5-PW 1e07feb4cc5cba17a649a04604bd7846
changed:        bruce.wayne@example1.noexist 20170306
mnt-by:         MAINT-EXAMPLE-1-AP
source:         APNIC',
'MAINT-EXAMPLE-1-AP'),

(12, 1, 1329434477, 9, 'mntner:         MAINT-EXAMPLE-2-AP
upd-to:         hostmaster@example2.noexist
descr:          Example 2
admin-c:        TSE2-AP
tech-c:         TSE2-AP
auth:           MD5-PW a2d4aa31b0c607dfb6f8b9f9be129a27
mnt-by:         MAINT-EXAMPLE-2-AP
referral-by:    APNIC-HM
changed:        tony.stark@example2.noexist 20170322
changed:        hm-changed@apnic.net 20170321
source:         APNIC',
'MAINT-EXAMPLE-2-AP'),

(13, 1, 1329434477, 10, 'person:         Bruce Wayne
address:      Example 1
address:      Brisbane, Australia
country:      AU
phone:        +61 0123456655
e-mail:       bruce.wayne@example1.noexist
nic-hdl:      BWE1-AP
mnt-by:       MAINT-EXAMPLE-1-AP
changed:      bruce.wayne@example1.noexist 201770306
source:       APNIC', 'BWE1-AP'),

(14, 1, 1329434477, 10, 'person:         Tony Stark
nic-hdl:      TSE2-AP
e-mail:       tony.stark@example2.noexist
address:      Example 2
address:      Brisbane, Australia
country:      AU
phone:        +61 0123456656
changed:      tony.stark@example2.noexist 20170223
mnt-by:       MAINT-EXAMPLE-2-AP
source:       APNIC', 'TSE2-AP'),

(15, 1, 1329434477, 11, 'role:         Example1 Administrator
address:        1 Queen St
address:        Brisbane Queensland 4000
country:        AU
phone:          +61 0123456655
e-mail:         admin@example1.noexist
admin-c:        BWE1-AP
tech-c:         BWE1-AP
nic-hdl:        EX1-AP
mnt-by:         MAINT-EXAMPLE-1-AP
changed:        hm-changed@apnic.net 20170327
source:         APNIC', 'EX1-AP'),

(16, 1, 1329434477, 11, 'role:         Example2 Administrator
address:        2 Queen St
address:        Brisbane Queensland 4000
country:        AU
phone:          +61 0123456656
e-mail:         admin@example2.noexist
changed:        hostmaster@pacific.net.id 20170228
admin-c:        TSE2-AP
tech-c:         TSE2-AP
nic-hdl:        EX2-AP
mnt-by:         MAINT-EXAMPLE-2-AP
source:         APNIC', 'EX2-AP'),

(17, 1, 1329434477, 17, 'irt:         IRT-EXAMPLE-1-AU
address:        1 Queen St
address:        Brisbane Queensland 4000
e-mail:         irt@example1.noexist
abuse-mailbox:  abuse@example1.noexist
admin-c:        BWE1-AP
tech-c:         BWE1-AP
auth:           MD5-PW 473c9dff8ec9cfc29111399b1c8e57d3
mnt-by:         MAINT-EXAMPLE-1-AP
changed:        bruce.wayne@example1.noexist 20170318
source:         APNIC', 'IRT-EXAMPLE-1-AU'),

(18, 1, 1329434477, 17, 'irt:         IRT-EXAMPLE-2-AU
address:        2 Queen St
address:        Brisbane Queensland 4000
e-mail:         irt@example2.noexist
abuse-mailbox:  abuse@example2.noexist
admin-c:        BWE2-AP
tech-c:         BWE2-AP
auth:           MD5-PW 2d892959d6eb9efcf0b551bfe46e6908
mnt-by:         MAINT-EXAMPLE-2-AP
changed:        tony.stark@example2.noexist 20101108
source:         APNIC', 'IRT-EXAMPLE-2-AU'),

(19, 1, 1329434477, 5, 'inet6num:        ffee:200:102::/48
netname:      EXAMP3IPV6
country:      AU
admin-c:      TSE2-AP
tech-c:       EX2-AP
status:       ASSIGNED NON-PORTABLE
mnt-by:       MAINT-EXAMPLE-2-AP
mnt-irt:      IRT-EXAMPLE-2-AU
changed:      admin@example2.noexist 20170321
source:       APNIC', 'ffee:200:102::/48');

INSERT INTO rdapd_test.serials (thread_id, serial_id, object_id, sequence_id,
                                 atlast, operation)
VALUES
(0, 1, 1, 1, 0, 0),
(0, 2, 2, 1, 0, 0),
(0, 3, 3, 1, 0, 0),
(0, 4, 4, 1, 0, 0),
(0, 5, 5, 1, 0, 0),
(0, 6, 6, 1, 0, 0),
(0, 7, 7, 1, 0, 0),
(0, 8, 8, 1, 0, 0),
(0, 9, 9, 1, 0, 0),
(0, 10, 10, 1, 0, 0),
(0, 11, 11, 1, 0, 0),
(0, 12, 12, 1, 0, 0),
(0, 13, 13, 1, 0, 0),
(0, 14, 14, 1, 0, 0),
(0, 15, 14, 1, 0, 0),
(0, 16, 14, 1, 0, 0),
(0, 17, 14, 1, 0, 0),
(0, 18, 14, 1, 0, 0),
(0, 19, 14, 1, 0, 0);

